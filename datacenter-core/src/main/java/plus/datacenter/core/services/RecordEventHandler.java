package plus.datacenter.core.services;

import plus.datacenter.core.entities.forms.Record;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface RecordEventHandler {

    Mono<Collection<Record>> onEvent(Collection<Record> records, EventType eventType);

    enum EventType {
        CREATE,
        UPDATE,
        DELETE
    }
}
