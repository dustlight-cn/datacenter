package plus.datacenter.mongo;

import com.mongodb.reactivestreams.client.MongoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import plus.datacenter.mongo.serializers.ObjectIdToStringSerializer;
import plus.datacenter.mongo.services.MongoFormRecordService;
import plus.datacenter.mongo.services.MongoFormService;

@Configuration
@EnableConfigurationProperties(DatacenterMongoProperties.class)
public class DatacenterMongoConfiguration {

    @Bean
    @ConditionalOnBean(ReactiveMongoOperations.class)
    public MongoFormService mongoFormService(@Autowired DatacenterMongoProperties properties,
                                             @Autowired ReactiveMongoOperations operations,
                                             @Autowired MongoClient mongoClient) {
        return new MongoFormService(mongoClient, operations, properties.getFormCollection());
    }

    @Bean
    @ConditionalOnBean(value = {ReactiveMongoOperations.class})
    @Primary
    public MongoFormRecordService mongoFormRecordService(@Autowired DatacenterMongoProperties properties,
                                                         @Autowired ReactiveMongoOperations operations,
                                                         @Autowired MongoClient mongoClient) {
        return new MongoFormRecordService(mongoClient,
                operations,
                properties.getFormRecordCollection());
    }

//    @Bean
//    @ConditionalOnProperty(prefix = "plus.datacenter.mongo", name = "enqueue", havingValue = "true", matchIfMissing = true)
//    public RabbitTemplate template(@Autowired ConnectionFactory factory,
//                                   @Autowired DatacenterMongoProperties properties) {
//        RabbitAdmin admin = new RabbitAdmin(factory);
//        Exchange exchange = new TopicExchange(properties.getExchange(),
//                true,
//                false);
//        admin.declareExchange(exchange);
//        RabbitTemplate template = new RabbitTemplate(factory);
//        template.setExchange(exchange.getName());
//        return template;
//    }

    @Bean
    public ObjectIdToStringSerializer objectIdToStringSerializer() {
        return new ObjectIdToStringSerializer();
    }
}
