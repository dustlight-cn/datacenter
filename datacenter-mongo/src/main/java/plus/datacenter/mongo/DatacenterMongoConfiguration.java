package plus.datacenter.mongo;

import com.mongodb.reactivestreams.client.MongoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import plus.datacenter.elasticsearch.services.ElasticsearchFormRecordService;
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
    @ConditionalOnBean(value = {ReactiveMongoOperations.class, ElasticsearchFormRecordService.class})
    @Primary
    public MongoFormRecordService mongoFormRecordService(@Autowired DatacenterMongoProperties properties,
                                                         @Autowired ReactiveMongoOperations operations,
                                                         @Autowired MongoClient mongoClient,
                                                         @Autowired ElasticsearchFormRecordService elasticsearchFormRecordService) {
        return new MongoFormRecordService(operations,
                properties.getFormRecordCollection(),
                elasticsearchFormRecordService,
                mongoClient);
    }

    @Bean
    @ConditionalOnBean(value = {ReactiveMongoOperations.class})
    public MongoFormRecordService mongoFormRecordService(@Autowired DatacenterMongoProperties properties,
                                                         @Autowired ReactiveMongoOperations operations,
                                                         @Autowired MongoClient mongoClient) {
        return new MongoFormRecordService(operations,
                properties.getFormRecordCollection(),
                null,
                mongoClient);
    }
}
