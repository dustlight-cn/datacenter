package plus.datacenter.core.services;

import plus.datacenter.core.entities.forms.FormRecord;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.HashSet;

public abstract class AbstractFormRecordService implements FormRecordService {

    private Collection<RecordEventHandler> joins = new HashSet<>();
    private Collection<RecordEventHandler> notifiers = new HashSet<>();

    protected Mono<Void> join(FormRecord record, RecordEventHandler.EventType eventType) {
        Mono<Void> result = Mono.empty();
        for (RecordEventHandler handler : joins) {
            Mono<Void> tmp = handler.onEvent(record, eventType);
            result.then(tmp);
        }
        return result;
    }

    protected Mono<Void> notify(FormRecord record, RecordEventHandler.EventType eventType) {
        Collection<Mono<Void>> results = new HashSet<>();
        for (RecordEventHandler notifier : notifiers) {
            results.add(notifier.onEvent(record, eventType));
        }
        return Mono.zip(results, objects -> null);
    }

    public void addEventHandler(RecordEventHandler handler) {
        this.joins.add(handler);
    }

    public void removeEventHandler(RecordEventHandler handler) {
        this.joins.remove(handler);
    }

    public void addNotifier(RecordEventHandler handler) {
        this.notifiers.add(handler);
    }

    public void removeNotifier(RecordEventHandler handler) {
        this.notifiers.remove(handler);
    }
}
