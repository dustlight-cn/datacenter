package plus.datacenter.core.services;

import plus.datacenter.core.entities.forms.FormRecord;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface FormRecordService {

    Mono<FormRecord> createRecord(FormRecord origin, String clientId);

    Flux<FormRecord> createRecords(Collection<FormRecord> origin, String clientId);

    Mono<FormRecord> getRecord(String id, String clientId);

    Flux<FormRecord> getRecords(Collection<String> id, String clientId);

    Mono<Void> updateRecord(FormRecord target, String clientId);

    Mono<Void> updateRecords(Collection<String> ids, FormRecord target, String clientId);

    Mono<Void> deleteRecord(String id, String clientId);

    Mono<Void> deleteRecords(Collection<String> ids, String clientId);

}
