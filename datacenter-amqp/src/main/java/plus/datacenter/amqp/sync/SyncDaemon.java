package plus.datacenter.amqp.sync;

import com.rabbitmq.client.Channel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import plus.datacenter.amqp.entities.RecodeEventMessage;
import plus.datacenter.core.DatacenterException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SyncDaemon implements ApplicationRunner, ChannelAwareMessageListener, HealthIndicator {

    private ConnectionFactory connectionFactory;
    private String exchange;
    private String queue, deadQueue, deadRoutingKey;

    private SimpleMessageListenerContainer container;

    private Collection<SyncHandler> handlers;

    private Log logger = LogFactory.getLog(getClass());

    public SyncDaemon(ConnectionFactory factory,
                      String exchange,
                      String queue,
                      String deadQueue,
                      String deadRoutingKey,
                      Collection<SyncHandler> handlers) {
        this.connectionFactory = factory;
        this.exchange = exchange;
        this.queue = queue;
        this.deadQueue = deadQueue;
        this.deadRoutingKey = deadRoutingKey;
        this.handlers = handlers;
        if (handlers == null || handlers.size() == 0)
            throw new DatacenterException("Sync handler can not be empty");
        if (connectionFactory == null)
            throw new DatacenterException("Connection factory muse be set");
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        init();
        container = new SimpleMessageListenerContainer(connectionFactory);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setQueueNames(queue);
        container.setMessageListener(this);
        container.start();
    }

    public boolean isActive() {
        return container == null ? false : container.isActive();
    }

    private void init() {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);

        // 死信队列
        Queue syncDeadQueue = new Queue(deadQueue, true, false, false, null);

        // 同步队列
        Map<String, Object> queueArgs = new HashMap<>();
        queueArgs.put("x-dead-letter-exchange", exchange);
        queueArgs.put("x-dead-letter-routing-key", deadRoutingKey);
        Queue syncQueue = new Queue(queue, true, false, false, queueArgs);
        Binding binding = new Binding(queue, Binding.DestinationType.QUEUE, exchange, "*.*.*", null);

        admin.declareQueue(syncDeadQueue);
        admin.declareQueue(syncQueue);
        admin.declareBinding(binding);
    }

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        RecodeEventMessage recordEvent = RecodeEventMessage.fromJson(message.getBody());
        if (recordEvent == null || recordEvent.getRecords() == null || recordEvent.getRecords().size() == 0) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        try {
            for (SyncHandler handler : handlers) {
                handler.sync(recordEvent).block();
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }

    @Override
    public Health health() {
        return this.isActive() ? Health.up().build() : Health.down().build();
    }
}
