package plus.datacenter.core.services;

import plus.datacenter.core.entities.forms.FormRecord;
import reactor.core.publisher.Mono;

public interface FormRecordService {

    Mono<FormRecord> createRecord(FormRecord origin);

    Mono<FormRecord> getRecord(String id);

    Mono<FormRecord> updateRecord(FormRecord target);

    Mono<Void> deleteRecord(String id);

}
