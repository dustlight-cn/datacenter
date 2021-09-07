package plus.datacenter.core.services;

import plus.datacenter.core.entities.forms.FormRecord;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface RecordEventHandler {

    Mono<Collection<FormRecord>> onEvent(Collection<FormRecord> records, EventType eventType);

    enum EventType {
        CREATE,
        UPDATE,
        DELETE
    }
}
