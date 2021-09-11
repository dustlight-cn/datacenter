package plus.datacenter.core.services;

import org.springframework.core.Ordered;
import plus.datacenter.core.entities.forms.Record;
import reactor.core.publisher.Mono;

import java.util.Collection;

/**
 * 记录事件处理器，处理记录的增删改事件。
 * <p>
 * 继承了 Ordered 接口，表示在执行发布事件时的顺序。
 */
public interface RecordEventHandler extends Ordered {

    /**
     * 记录的增删改事件触发。
     */
    Mono<Collection<Record>> onEvent(Collection<Record> records, EventType eventType);

    enum EventType {
        CREATE,
        UPDATE,
        DELETE
    }
}
