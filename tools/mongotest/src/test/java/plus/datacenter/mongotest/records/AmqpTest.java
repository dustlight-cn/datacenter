package plus.datacenter.mongotest.records;

import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import plus.datacenter.amqp.entities.RecodeEventMessage;
import plus.datacenter.core.entities.forms.Record;
import plus.datacenter.core.services.EnhancedRecordService;

import java.util.Collection;
import java.util.List;

@SpringBootTest
public class AmqpTest {

    private static final Gson gson = Converters.registerInstant(new GsonBuilder()).create();

    @Autowired
    ConnectionFactory connectionFactory;

    Log logger = LogFactory.getLog(getClass());

    @Autowired
    EnhancedRecordService enhancedRecordService;

    @Test
    public void listen() throws InterruptedException {
        String exchange = "datacenter-test";
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        Queue tmpQueue = admin.declareQueue();

        Binding binding = new Binding(tmpQueue.getActualName(), Binding.DestinationType.QUEUE, exchange, "*.*.*", null);
        admin.declareBinding(binding);

        SimpleMessageListenerContainer listenerContainer = new SimpleMessageListenerContainer(connectionFactory);
        listenerContainer.setQueues(tmpQueue);

        listenerContainer.setAcknowledgeMode(AcknowledgeMode.MANUAL);

        listenerContainer.setMessageListener((ChannelAwareMessageListener) (msg, channel) -> {
            try {
                RecodeEventMessage event = gson.fromJson(new String(msg.getBody()), RecodeEventMessage.class);
                if (event.getRecords().size() == 0) {
                    channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
                    return;
                }
                Collection<Record> records = event.getRecords();
                String clientID = records.iterator().next().getClientId();

                List<Record> targets = enhancedRecordService.searchAssociatedRecords(records, clientID)
                        .collectList().block();
                targets.addAll(records);
                logger.info(targets);
                List<Record> fullRecords = enhancedRecordService.getFullRecords(targets, clientID).collectList().block();

                logger.info(gson.toJson(fullRecords));
            } catch (Throwable t) {
                channel.basicNack(msg.getMessageProperties().getDeliveryTag(), false, false);
                logger.error(t.getMessage(), t);
            }
        });
        listenerContainer.start();
        synchronized (listenerContainer) {
            listenerContainer.wait();
        }

    }
}
