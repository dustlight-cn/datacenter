package plus.datacenter.core.services;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import plus.datacenter.core.DatacenterException;
import plus.datacenter.core.entities.forms.Record;
import reactor.core.publisher.Mono;

import java.util.Collection;

/**
 * 抽象表单事件处理器，将事件进行分发到三个抽象方法上。
 */
@AllArgsConstructor
@NoArgsConstructor
public abstract class AbstractRecordEventHandler implements RecordEventHandler {

    private int order;

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


    @Override
    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    /**
     * 创建事件
     *
     * @param records 被创建的记录集合
     * @return 返回的记录集合将会应用于下一个 Handler 以及最终返回值
     */
    protected abstract Mono<Collection<Record>> onCreate(Collection<Record> records);

    /**
     * 更新事件
     *
     * @param records 被更新的记录集合
     * @return 返回的记录集合将会应用于下一个 Handler 以及最终返回值
     */
    protected abstract Mono<Collection<Record>> onUpdate(Collection<Record> records);

    /**
     * 删除事件
     *
     * @param records 被删除的记录集合
     * @return 返回的记录集合将会应用于下一个 Handler 以及最终返回值
     */
    protected abstract Mono<Collection<Record>> onDelete(Collection<Record> records);
}
