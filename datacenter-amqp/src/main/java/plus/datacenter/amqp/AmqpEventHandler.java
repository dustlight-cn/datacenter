package plus.datacenter.amqp;

import lombok.Getter;
import lombok.Setter;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import plus.datacenter.core.entities.forms.Record;
import plus.datacenter.core.services.RecordEventHandler;
import reactor.core.publisher.Mono;

import java.util.*;

public class AmqpEventHandler implements RecordEventHandler, InitializingBean {

    private int order = 0;

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
        return Mono
                .fromRunnable(() -> template.convertAndSend(getPrimaryKey(),
                        RecodeEventMessage.create(eventType, records).toJson()))
                .then(Mono.just(records));
//        return Mono.fromRunnable(() -> {
//            Map<String, Map<String, Collection<Record>>> classifiedRecords = classify(records);
//            Iterator<Map.Entry<String, Map<String, Collection<Record>>>> clientIdIter = classifiedRecords.entrySet().iterator();
//            while () ;
//
//            template.convertAndSend(computeRoutingKey()
//        }));
    }

    protected Map<String, Map<String, Collection<Record>>> classify(Collection<Record> records) {
        if (records == null || records.size() == 0)
            return Collections.emptyMap();
        Map<String, Map<String, Collection<Record>>> result = new HashMap<>();
        for (Record record : records) {
            String clientId = record.getClientId();
            String formName = record.getFormName();
            Map<String, Collection<Record>> innerMap = result.get(clientId);
            if (innerMap == null)
                result.put(clientId, (innerMap = new HashMap<>()));
            Collection<Record> innerRecords = innerMap.get(formName);
            if (innerRecords == null)
                innerMap.put(formName, (innerRecords = new HashSet<>()));
            innerRecords.add(record);
        }
        return result;
    }

    protected String getPrimaryKey() {
        return "PRIMARY";
    }

    protected String computeRoutingKey(Record record, EventType eventType) {
        return String.format("%s:%s:%s", eventType.toString(), record.getClientId(), record.getFormName());
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
