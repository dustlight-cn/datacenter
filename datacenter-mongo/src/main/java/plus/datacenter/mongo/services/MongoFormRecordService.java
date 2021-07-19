package plus.datacenter.mongo.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.BasicUpdate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.StringUtils;
import plus.datacenter.core.ErrorEnum;
import plus.datacenter.core.entities.forms.FormRecord;
import plus.datacenter.core.services.FormRecordService;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class MongoFormRecordService implements FormRecordService {

    private ReactiveMongoOperations operations;
    private String collectionName;
    private static ObjectMapper mapper = new ObjectMapper();

    @Override
    public Mono<FormRecord> createRecord(FormRecord origin) {
        if (!StringUtils.hasText(origin.getFormId()))
            ErrorEnum.CREATE_FORM_FAILED.details("form id can't not be null!").throwException();
        origin.setId(null);
        Instant t = Instant.now();
        origin.setCreatedAt(t);
        origin.setUpdatedAt(t);
        return operations.insert(origin, collectionName)
                .onErrorMap(throwable -> ErrorEnum.CREATE_RESOURCE_FAILED.details(throwable.getMessage()).getException())
                ;
    }

    @Override
    public Mono<FormRecord> getRecord(String id) {
        return operations.findById(id, FormRecord.class, collectionName)
                .onErrorMap(throwable -> ErrorEnum.RESOURCE_NOT_FOUND.details(throwable.getMessage()).getException())
                .switchIfEmpty(Mono.error(ErrorEnum.RESOURCE_NOT_FOUND.getException()));
    }

    @Override
    public Mono<FormRecord> updateRecord(FormRecord target) {
        try {
            Document doc = Document.parse(mapper.writeValueAsString(target));

            Update update = new BasicUpdate(doc);
            return operations.findAndModify(Query.query(Criteria.where("_id").is(target.getId())),
                    update,
                    FormRecord.class,
                    collectionName)
                    .onErrorMap(throwable -> ErrorEnum.UPDATE_RESOURCE_FAILED.details(throwable.getMessage()).getException())
                    .switchIfEmpty(Mono.error(ErrorEnum.RESOURCE_NOT_FOUND.getException()));
        } catch (JsonProcessingException e) {
            throw ErrorEnum.UPDATE_RESOURCE_FAILED.details(e.getMessage()).getException();
        }
    }

    @Override
    public Mono<Void> deleteRecord(String id) {
        return operations.findAndRemove(Query.query(Criteria.where("_id").is(id)),
                FormRecord.class,
                collectionName)
                .onErrorMap(throwable -> ErrorEnum.DELETE_RESOURCE_FAILED.details(throwable.getMessage()).getException())
                .switchIfEmpty(Mono.error(ErrorEnum.RESOURCE_NOT_FOUND.getException()))
                .then();
    }
}
