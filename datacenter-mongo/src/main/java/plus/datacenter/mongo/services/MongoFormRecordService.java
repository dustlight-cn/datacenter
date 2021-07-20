package plus.datacenter.mongo.services;

import com.mongodb.reactivestreams.client.MongoClient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.StringUtils;
import plus.datacenter.core.DatacenterException;
import plus.datacenter.core.ErrorEnum;
import plus.datacenter.core.entities.forms.FormRecord;
import plus.datacenter.core.services.FormRecordService;
import plus.datacenter.elasticsearch.services.ElasticsearchFormRecordService;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Iterator;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class MongoFormRecordService implements FormRecordService {

    private ReactiveMongoOperations operations;
    private String collectionName;
    private ElasticsearchFormRecordService elasticsearchFormRecordService;
    private MongoClient mongoClient;

    @Override
    public Mono<FormRecord> createRecord(FormRecord origin) {
        if (!StringUtils.hasText(origin.getFormId()))
            ErrorEnum.CREATE_FORM_FAILED.details("form id can't not be null!").throwException();
        origin.setId(null);
        Instant t = Instant.now();
        origin.setCreatedAt(t);
        origin.setUpdatedAt(t);
        return Mono.from(mongoClient.startSession())
                .flatMap(clientSession -> {
                    clientSession.startTransaction();
                    return Mono.just(clientSession);
                })
                .flatMap(clientSession -> operations.withSession(clientSession)
                        .insert(origin, collectionName)
                        .onErrorMap(throwable -> ErrorEnum.CREATE_RESOURCE_FAILED.details(throwable.getMessage()).getException())
                        .flatMap(record -> elasticsearchFormRecordService == null ? Mono.from(clientSession.commitTransaction()).then(Mono.just(record)) :
                                elasticsearchFormRecordService.createRecord(record).flatMap(record1 -> Mono.from(clientSession.commitTransaction()).then(Mono.just(record1))))
                        .onErrorResume(throwable -> Mono.from(clientSession.abortTransaction()).then(Mono.error(throwable)))
                        .doFinally(signalType -> clientSession.close())
                );
    }

    @Override
    public Mono<FormRecord> getRecord(String id) {
        return operations.findById(id, FormRecord.class, collectionName)
                .onErrorMap(throwable -> ErrorEnum.RESOURCE_NOT_FOUND.details(throwable.getMessage()).getException())
                .switchIfEmpty(Mono.error(ErrorEnum.RESOURCE_NOT_FOUND.getException()));
    }

    @Override
    public Mono<FormRecord> updateRecord(FormRecord target) {
        target.setUpdatedAt(Instant.now());
        Update update = new Update();
        update.set("updatedAt", Instant.now());
        Map<String, Object> data = target.getData();
        if (data != null) {
            Iterator<Map.Entry<String, Object>> iterator = data.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> kv = iterator.next();
                if (kv.getValue() != null)
                    update.set("data." + kv.getKey(), kv.getValue());
            }
        }

        return Mono.from(mongoClient.startSession())
                .flatMap(clientSession -> {
                    clientSession.startTransaction();
                    return Mono.just(clientSession);
                })
                .flatMap(clientSession -> operations.withSession(clientSession)
                        .findAndModify(Query.query(Criteria.where("_id").is(target.getId())),
                                update,
                                FormRecord.class,
                                collectionName)
                        .switchIfEmpty(Mono.error(ErrorEnum.RESOURCE_NOT_FOUND.getException()))
                        .map(record -> {
                            target.setFormId(record.getFormId());
                            target.setFormName(record.getFormName());
                            target.setFormVersion(record.getFormVersion());
                            return record;
                        })
                        .flatMap(record -> elasticsearchFormRecordService == null ? Mono.from(clientSession.commitTransaction()).then(Mono.just(record)) :
                                elasticsearchFormRecordService.updateRecord(target).flatMap(record1 -> Mono.from(clientSession.commitTransaction()).then(Mono.just(record))))
                        .onErrorMap(throwable -> throwable instanceof DatacenterException ? throwable : ErrorEnum.UPDATE_RESOURCE_FAILED.details(throwable.getMessage()).getException())
                        .onErrorResume(throwable -> Mono.from(clientSession.abortTransaction()).then(Mono.error(throwable)))
                        .doFinally(signalType -> clientSession.close())
                );
    }

    @Override
    public Mono<Void> deleteRecord(String id) {
        return Mono.from(mongoClient.startSession())
                .flatMap(clientSession -> {
                    clientSession.startTransaction();
                    return Mono.just(clientSession);
                })
                .flatMap(clientSession -> operations.withSession(clientSession)
                        .findAndRemove(Query.query(Criteria.where("_id").is(id)),
                                FormRecord.class,
                                collectionName)
                        .onErrorMap(throwable -> ErrorEnum.DELETE_RESOURCE_FAILED.details(throwable.getMessage()).getException())
                        .switchIfEmpty(Mono.error(ErrorEnum.RESOURCE_NOT_FOUND.getException()))
                        .flatMap(record -> elasticsearchFormRecordService == null ? Mono.from(clientSession.commitTransaction()) :
                                elasticsearchFormRecordService.deleteRecord(id)
                                        .then(Mono.from(clientSession.commitTransaction())))
                        .onErrorResume(throwable -> Mono.from(clientSession.abortTransaction()).then(Mono.error(throwable)))
                        .doFinally(signalType -> clientSession.close())
                );
    }
}
