package plus.datacenter.mongo.services;

import com.mongodb.reactivestreams.client.ClientSession;
import com.mongodb.reactivestreams.client.MongoClient;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.util.StringUtils;
import plus.datacenter.core.ErrorEnum;
import plus.datacenter.core.entities.forms.FormRecord;
import plus.datacenter.core.services.AbstractFormRecordService;
import reactor.core.publisher.Mono;

import java.util.Collection;

@Getter
@Setter
public class NewMongoFormRecordService extends AbstractFormRecordService {

    private MongoClient mongoClient;
    private ReactiveMongoOperations operations;

    private String collectionName;

    @Override
    public Mono<FormRecord> createRecord(FormRecord origin) {
        if(origin == null)
            ErrorEnum.CREATE_RECORD_FAILED.details("Form ID must be set").throwException();
        if(!StringUtils.hasText(origin.getFormId()));
        return null;
    }

    @Override
    public Mono<FormRecord> getRecord(String id, String clientId) {
        return null;
    }

    @Override
    public Mono<FormRecord> updateRecord(FormRecord target) {
        return null;
    }

    @Override
    public Mono<Void> deleteRecord(String id, String clientId) {
        return null;
    }

    @Override
    public Mono<Void> deleteRecords(Collection<String> ids, String clientId) {
        return null;
    }

    protected Mono<ClientSession> startTransaction() {
        return Mono.from(mongoClient.startSession())
                .flatMap(clientSession -> {
                    clientSession.startTransaction();
                    return Mono.just(clientSession);
                });
    }
}
