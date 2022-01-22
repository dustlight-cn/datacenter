package cn.dustlight.datacenter.amqp.sync;

import com.rabbitmq.client.Channel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.beans.BeansException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import cn.dustlight.datacenter.amqp.entities.RecodeEvent;
import cn.dustlight.datacenter.core.DatacenterException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SyncDaemon implements ApplicationRunner, ChannelAwareMessageListener, HealthIndicator, ApplicationContextAware {

    private ConnectionFactory connectionFactory;
    private String exchange;
    private String queue, deadQueue, deadRoutingKey;

    private SimpleMessageListenerContainer container;

    private Collection<SyncHandler> handlers;

    private Log logger = LogFactory.getLog(getClass());
    private ApplicationContext applicationContext;

    public SyncDaemon(ConnectionFactory factory,
                      String exchange,
                      String queue,
                      String deadQueue,
                      String deadRoutingKey) {
        this.connectionFactory = factory;
        this.exchange = exchange;
        this.queue = queue;
        this.deadQueue = deadQueue;
        this.deadRoutingKey = deadRoutingKey;

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
        this.handlers = applicationContext.getBeansOfType(SyncHandler.class).values();
        if (handlers == null || handlers.size() == 0)
            throw new DatacenterException("Sync handler can not be empty");

        RabbitAdmin admin = new RabbitAdmin(connectionFactory);

        // 死信队列
        Queue syncDeadQueue = new Queue(deadQueue, true, false, false, null);
        Binding deadBinding = new Binding(deadQueue, Binding.DestinationType.QUEUE, exchange, deadRoutingKey, null);

        // 同步队列
        Map<String, Object> queueArgs = new HashMap<>();
        queueArgs.put("x-dead-letter-exchange", exchange);
        queueArgs.put("x-dead-letter-routing-key", deadRoutingKey);
        Queue syncQueue = new Queue(queue, true, false, false, queueArgs);
        Binding binding = new Binding(queue, Binding.DestinationType.QUEUE, exchange, "*.*.*", null);

        admin.declareQueue(syncDeadQueue);
        admin.declareBinding(deadBinding);
        admin.declareQueue(syncQueue);
        admin.declareBinding(binding);
    }

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        try {
            RecodeEvent recordEvent = RecodeEvent.fromJson(message.getBody());
            if (recordEvent == null || recordEvent.getRecords() == null || recordEvent.getRecords().size() == 0) {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                return;
            }
            for (SyncHandler handler : handlers) {
                handler.sync(recordEvent).block();
            }
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
        }
    }

    @Override
    public Health health() {
        return this.isActive() ? Health.up().build() : Health.down().build();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
