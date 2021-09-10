package plus.datacenter.amqp;

import lombok.Getter;
import lombok.Setter;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import plus.datacenter.amqp.entities.RecodeEventMessage;
import plus.datacenter.core.DatacenterException;
import plus.datacenter.core.entities.forms.Record;
import plus.datacenter.core.services.RecordEventHandler;
import reactor.core.publisher.Mono;

import java.util.*;

public class AmqpEventHandler implements RecordEventHandler, InitializingBean {

    private int order;

    @Getter
    @Setter
    private RabbitTemplate template;

    public AmqpEventHandler() {
        this(null);
    }

    public AmqpEventHandler(RabbitTemplate template) {
        this(template, 0);
    }

    public AmqpEventHandler(RabbitTemplate template, int order) {
        this.template = template;
        this.order = order;
    }

    @Override
    public Mono<Collection<Record>> onEvent(Collection<Record> records, EventType eventType) {
        if (records == null || records.size() == 0)
            return Mono.just(records);
        Record recordTmp = pickUp(records);
        if (recordTmp == null)
            return Mono.error(new DatacenterException("One of record is null"));
        String routingKey = computeRoutingKey(recordTmp, eventType);
        return Mono
                .fromRunnable(() -> template.convertAndSend(routingKey,
                        RecodeEventMessage.create(eventType, records).toJson()))
                .then(Mono.just(records));
    }

    protected String computeRoutingKey(Record record, EventType eventType) {
        return String.format("%s.%s.%s", eventType.toString(), record.getClientId(), record.getFormName());
    }

    /**
     * 检查集合元素是否包含 null 值。
     *
     * @param objects
     * @return 若含有 null 值则返回 null，否则返回第一个元素。
     */
    private <T> T pickUp(Collection<T> objects) {
        if (objects == null || objects.size() == 0)
            return null;
        Iterator<T> iter = objects.iterator();
        T result = null;
        while (iter.hasNext()) {
            T tmp = iter.next();
            if (tmp == null)
                return null;
            else if (result == null)
                result = tmp;
        }
        return result;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(template, "Rabbit template must be set");
    }


}
