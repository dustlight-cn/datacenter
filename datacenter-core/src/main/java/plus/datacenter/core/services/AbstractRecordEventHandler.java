package plus.datacenter.core.services;

import plus.datacenter.core.DatacenterException;
import plus.datacenter.core.entities.forms.FormRecord;
import reactor.core.publisher.Mono;

import java.util.Collection;

public abstract class AbstractRecordEventHandler implements RecordEventHandler {

    @Override
    public Mono<Collection<FormRecord>> onEvent(Collection<FormRecord> records, EventType eventType) {
        switch (eventType) {
            case CREATE:
                return onCreate(records);
            case UPDATE:
                return onUpdate(records);
            case DELETE:
                return onDelete(records);
            default:
                return Mono.error(new DatacenterException("Event type not found"));
        }
    }

    public abstract Mono<Collection<FormRecord>> onCreate(Collection<FormRecord> record);

    public abstract Mono<Collection<FormRecord>> onUpdate(Collection<FormRecord> record);

    public abstract Mono<Collection<FormRecord>> onDelete(Collection<FormRecord> record);
}
