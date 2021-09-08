package plus.datacenter.core.services;

import plus.datacenter.core.DatacenterException;
import plus.datacenter.core.entities.forms.Record;
import reactor.core.publisher.Mono;

import java.util.Collection;

public abstract class AbstractRecordEventHandler implements RecordEventHandler {

    @Override
    public Mono<Collection<Record>> onEvent(Collection<Record> records, EventType eventType) {
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

    public abstract Mono<Collection<Record>> onCreate(Collection<Record> record);

    public abstract Mono<Collection<Record>> onUpdate(Collection<Record> record);

    public abstract Mono<Collection<Record>> onDelete(Collection<Record> record);
}
