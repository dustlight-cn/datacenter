package plus.datacenter.amqp;

import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import plus.datacenter.amqp.properties.AmqpProperties;
import plus.datacenter.amqp.properties.SyncProperties;
import plus.datacenter.amqp.sync.SyncDaemon;
import plus.datacenter.amqp.sync.SyncHandler;

@Configuration
@EnableConfigurationProperties(value = {AmqpProperties.class, SyncProperties.class})
public class AmqpConfiguration {

    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    @ConditionalOnMissingBean
    public AmqpEventHandler amqpEventHandler(@Autowired ConnectionFactory factory,
                                             @Autowired AmqpProperties properties) {
        Assert.hasText(properties.getExchange(), "Exchange name can not be empty");
        RabbitAdmin admin = new RabbitAdmin(factory);
        Exchange exchange = new TopicExchange(properties.getExchange(),
                true,
                false);
        admin.declareExchange(exchange);
        RabbitTemplate template = new RabbitTemplate(factory);
        template.setExchange(exchange.getName());
        return new AmqpEventHandler(template);
    }

    @Bean
    @ConditionalOnProperty(prefix = "plus.datacenter.amqp.sync", name = "enabled", matchIfMissing = true)
    public SyncDaemon syncDaemon(@Autowired ConnectionFactory factory,
                                 @Autowired AmqpProperties properties) {
        return new SyncDaemon(factory,
                properties.getExchange(),
                properties.getSync().getQueue(),
                properties.getSync().getDeadLetterQueue(),
                properties.getSync().getDeadLetterRoutingKey());
    }
}
