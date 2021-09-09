package plus.datacenter.mongo;

import com.mongodb.reactivestreams.client.MongoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import plus.datacenter.core.services.RecordEventHandler;
import plus.datacenter.core.services.RecordValidator;
import plus.datacenter.mongo.serializers.ObjectIdToStringSerializer;
import plus.datacenter.mongo.services.MongoRecordService;
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
    public MongoRecordService mongoFormRecordService(@Autowired DatacenterMongoProperties properties,
                                                     @Autowired ReactiveMongoOperations operations,
                                                     @Autowired MongoClient mongoClient,
                                                     @Autowired ApplicationContext context) {
        MongoRecordService service = new MongoRecordService(mongoClient,
                operations,
                properties.getRecordCollection());
        service.addEventHandler(context.getBeansOfType(RecordEventHandler.class).values());
        service.addValidator(context.getBeansOfType(RecordValidator.class).values());
        return service;
    }

    @Bean
    public ObjectIdToStringSerializer objectIdToStringSerializer() {
        return new ObjectIdToStringSerializer();
    }
}
