package cn.dustlight.datacenter.mongo;

import cn.dustlight.datacenter.mongo.converters.FormValueTransformer;
import cn.dustlight.datacenter.mongo.converters.ObjectIdToStringSerializer;
import cn.dustlight.datacenter.mongo.services.MongoEnhancedRecordService;
import cn.dustlight.datacenter.mongo.services.MongoFormService;
import cn.dustlight.datacenter.mongo.services.MongoRecordService;
import com.mongodb.reactivestreams.client.MongoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import cn.dustlight.datacenter.core.services.FormValidator;
import cn.dustlight.datacenter.core.services.PrincipalHolder;
import cn.dustlight.datacenter.core.services.RecordEventHandler;
import cn.dustlight.datacenter.core.services.RecordValidator;

@Configuration
@EnableConfigurationProperties(DatacenterMongoProperties.class)
public class DatacenterMongoConfiguration {

    @Bean
    public MongoFormService mongoFormService(@Autowired DatacenterMongoProperties properties,
                                             @Autowired ReactiveMongoOperations operations,
                                             @Autowired MongoClient mongoClient,
                                             @Autowired PrincipalHolder principalHolder,
                                             @Autowired ApplicationContext applicationContext) {
        MongoFormService service = new MongoFormService(mongoClient, operations, properties.getFormCollection());
        service.setPrincipalHolder(principalHolder);
        if (properties.isAutoInjectValidators()) {
            service.addValidator(applicationContext.getBeansOfType(FormValidator.class).values());
        }
        return service;
    }

    @Bean
    public MongoRecordService mongoFormRecordService(@Autowired DatacenterMongoProperties properties,
                                                     @Autowired ReactiveMongoOperations operations,
                                                     @Autowired MongoClient mongoClient,
                                                     @Autowired PrincipalHolder principalHolder,
                                                     @Autowired ApplicationContext applicationContext) {
        MongoRecordService service = new MongoRecordService(mongoClient,
                operations,
                properties.getRecordCollection());
        service.setPrincipalHolder(principalHolder);

        if (properties.isAutoInjectHandlers()) {
            service.addEventHandler(applicationContext.getBeansOfType(RecordEventHandler.class).values());
        }
        if (properties.isAutoInjectValidators()) {
            service.addValidator(applicationContext.getBeansOfType(RecordValidator.class).values());
        }
        return service;
    }

    @Bean
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
