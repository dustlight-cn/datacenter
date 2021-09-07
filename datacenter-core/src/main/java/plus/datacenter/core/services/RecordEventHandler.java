package plus.datacenter.core.services;

import plus.datacenter.core.entities.forms.FormRecord;
import reactor.core.publisher.Mono;

public interface RecordEventHandler {

    Mono<Void> onEvent(FormRecord record, EventType eventType);

    enum EventType {
        CREATE,
        UPDATE,
        DELETE
    }
}
