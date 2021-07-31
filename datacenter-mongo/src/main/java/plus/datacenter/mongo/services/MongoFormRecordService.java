package plus.datacenter.mongo.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.reactivestreams.client.MongoClient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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

import java.io.Serializable;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class MongoFormRecordService implements FormRecordService {

    private static ObjectMapper objectMapper = new ObjectMapper();
    private ReactiveMongoOperations operations;
    private String collectionName;
    private ElasticsearchFormRecordService elasticsearchFormRecordService;
    private MongoClient mongoClient;
    private RabbitTemplate rabbitTemplate;

    @Override
    public Mono<FormRecord> createRecord(FormRecord origin) {
        if (!StringUtils.hasText(origin.getFormId()))
            ErrorEnum.CREATE_FORM_FAILED.details("form id can't not be null!").throwException();
        origin.setId(new ObjectId().toHexString());
        Instant t = Instant.now();
        if (origin.getCreatedAt() == null)
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
                        .flatMap(record -> elasticsearchFormRecordService == null ? Mono.just(record) :
                                elasticsearchFormRecordService.createRecord(record).flatMap(record1 -> Mono.just(record1)))
                        .flatMap(record -> Mono.fromRunnable(() -> rabbitTemplate.convertAndSend(getRouting(origin, RecordMessage.MessageType.CREATED),
                                RecordMessage.from(origin, RecordMessage.MessageType.CREATED).toJson()))
                                .then(Mono.just(record)))
                        .flatMap(record -> Mono.from(clientSession.commitTransaction()).then(Mono.just(record)))
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
        target.setUpdatedAt(Instant.now());
        target.setOwner(null);
        target.setClientId(null);
        if (target.getCreatedAt() != null)
            update.set("createdAt", target.getCreatedAt());
        update.set("updatedAt", target.getUpdatedAt());

        if (StringUtils.hasText(target.getFormId()))
            update.set("formId", target.getFormId());
        if (StringUtils.hasText(target.getFormName()))
            update.set("formName", target.getFormName());
        if (target.getFormVersion() != null)
            update.set("formVersion", target.getFormVersion());

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
                            if (StringUtils.hasText(record.getOwner()))
                                target.setOwner(record.getOwner());
                            target.setClientId(record.getClientId());
                            return target;
                        })
                        .flatMap(record -> elasticsearchFormRecordService == null ? Mono.just(record) :
                                elasticsearchFormRecordService.updateRecord(target).then(Mono.just(record)))
                        .flatMap(record -> Mono.fromRunnable(() -> rabbitTemplate.convertAndSend(getRouting(target, RecordMessage.MessageType.UPDATED),
                                RecordMessage.from(target, RecordMessage.MessageType.UPDATED).toJson()))
                                .then(Mono.just(record)))
                        .flatMap(record -> Mono.from(clientSession.commitTransaction()).then(Mono.just(record)))
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
                        .flatMap(record -> elasticsearchFormRecordService == null ? Mono.just(record) :
                                elasticsearchFormRecordService.deleteRecord(id).then(Mono.just(record)))
                        .flatMap(record -> Mono.fromRunnable(() -> rabbitTemplate.convertAndSend(getRouting(record, RecordMessage.MessageType.DELETED),
                                RecordMessage.from(record, RecordMessage.MessageType.DELETED).toJson()))
                                .then(Mono.just(record)))
                        .flatMap(v -> Mono.from(clientSession.commitTransaction()))
                        .onErrorResume(throwable -> Mono.from(clientSession.abortTransaction()).then(Mono.error(throwable)))
                        .doFinally(signalType -> clientSession.close())
                );
    }

    @Override
    public Mono<Void> deleteRecords(Collection<String> ids) {
        return Mono.from(mongoClient.startSession())
                .flatMap(clientSession -> {
                    clientSession.startTransaction();
                    return Mono.just(clientSession);
                })
                .flatMap(clientSession -> operations.withSession(clientSession)
                        .findAndRemove(Query.query(Criteria.where("_id").in(ids)),
                                FormRecord.class,
                                collectionName)
                        .onErrorMap(throwable -> ErrorEnum.DELETE_RESOURCE_FAILED.details(throwable.getMessage()).getException())
                        .switchIfEmpty(Mono.error(ErrorEnum.RESOURCE_NOT_FOUND.getException()))
                        .flatMap(record -> elasticsearchFormRecordService == null ? Mono.just(record) :
                                elasticsearchFormRecordService.deleteRecords(ids).then(Mono.just(record)))
                        .flatMap(record -> Mono.fromRunnable(() -> rabbitTemplate.convertAndSend(getRouting(record, RecordMessage.MessageType.DELETED),
                                RecordMessage.from(record, RecordMessage.MessageType.DELETED).toJson()))
                                .then(Mono.just(record)))
                        .flatMap(v -> Mono.from(clientSession.commitTransaction()))
                        .onErrorResume(throwable -> Mono.from(clientSession.abortTransaction()).then(Mono.error(throwable)))
                        .doFinally(signalType -> clientSession.close())
                );
    }

    private static String getRouting(FormRecord formRecord, RecordMessage.MessageType type) {
        if (formRecord == null)
            return null;
        return String.format("%s.%s.%s", type.toString(), formRecord.getClientId(), formRecord.getFormName());
    }

    @Getter
    @Setter
    public static class RecordMessage implements Serializable {

        private String recordId, formName, formId, clientId, owner;
        private Integer formVersion;
        private Map<String, Object> updateData;

        private MessageType type;

        public static RecordMessage from(FormRecord record, MessageType type) {
            if (record == null)
                return null;
            RecordMessage msg = new RecordMessage();
            msg.setFormId(record.getFormId());
            msg.setFormName(record.getFormName());
            msg.setFormVersion(record.getFormVersion());
            msg.setRecordId(record.getId());
            msg.setClientId(record.getClientId());
            msg.setOwner(record.getOwner());
            msg.setType(type);
            if (type == MessageType.UPDATED) {
                Map<String, Object> data = record.getData();
                Iterator<Map.Entry<String, Object>> iterator = data.entrySet().iterator();
                Map<String, Object> updateData = new HashMap<>();
                while (iterator.hasNext()) {
                    Map.Entry<String, Object> kv = iterator.next();
                    if (kv.getValue() == null)
                        continue;
                    updateData.put(kv.getKey(), kv.getValue());
                }
                msg.setUpdateData(updateData);
            }
            return msg;
        }

        public String toJson() {
            try {
                return objectMapper.writeValueAsString(this);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Fail to convert RecordMessage to json string", e);
            }
        }

        public enum MessageType {
            CREATED,
            UPDATED,
            DELETED
        }
    }
}
