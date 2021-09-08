package plus.datacenter.core.services;

import plus.datacenter.core.entities.forms.Record;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface RecordService {

    Mono<Record> createRecord(Record origin, String clientId);

    Flux<Record> createRecords(Collection<Record> origin, String clientId);

    Mono<Record> getRecord(String id, String clientId);

    Flux<Record> getRecords(Collection<String> id, String clientId);

    Mono<Void> updateRecord(Record target, String clientId);

    Mono<Void> updateRecords(Collection<String> ids, Record target, String clientId);

    Mono<Void> deleteRecord(String id, String clientId);

    Mono<Void> deleteRecords(Collection<String> ids, String clientId);

}
