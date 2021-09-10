package plus.datacenter.mongo;

import com.mongodb.reactivestreams.client.MongoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import plus.datacenter.core.services.PrincipalHolder;
import plus.datacenter.mongo.converters.FormValueTransformer;
import plus.datacenter.mongo.converters.ObjectIdToStringSerializer;
import plus.datacenter.mongo.services.MongoEnhancedRecordService;
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
    @ConditionalOnBean(value = {ReactiveMongoOperations.class, PrincipalHolder.class})
    public MongoRecordService mongoFormRecordService(@Autowired DatacenterMongoProperties properties,
                                                     @Autowired ReactiveMongoOperations operations,
                                                     @Autowired MongoClient mongoClient,
                                                     @Autowired PrincipalHolder principalHolder) {
        MongoRecordService service = new MongoRecordService(mongoClient,
                operations,
                properties.getRecordCollection());
        service.setPrincipalHolder(principalHolder);
        return service;
    }

    @Bean
    @ConditionalOnBean(value = {ReactiveMongoOperations.class})
    public MongoEnhancedRecordService mongoEnhancedRecordService(@Autowired DatacenterMongoProperties properties,
                                                                 @Autowired ReactiveMongoOperations operations) {
        return new MongoEnhancedRecordService(operations, properties.getRecordCollection(), properties.getFormCollection());
    }

    @Bean
    public ObjectIdToStringSerializer objectIdToStringSerializer() {
        return new ObjectIdToStringSerializer();
    }

    @Bean
    public FormValueTransformer formValueTransformer() {
        return new FormValueTransformer();
    }
}
