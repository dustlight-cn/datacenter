package plus.datacenter.elasticsearch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import plus.datacenter.elasticsearch.services.ElasticsearchFormRecordService;
import plus.datacenter.elasticsearch.services.ElasticsearchFormSearcher;

@EnableConfigurationProperties(DatacenterElasticsearchProperties.class)
@Configuration
public class DatacenterElasticsearchConfiguration {

    @Bean
    @ConditionalOnBean(ReactiveElasticsearchOperations.class)
    public ElasticsearchFormSearcher elasticsearchFormSearcher(@Autowired ReactiveElasticsearchOperations operations) {
        return new ElasticsearchFormSearcher(operations);
    }

    @Bean
    @ConditionalOnBean(ReactiveElasticsearchOperations.class)
    public ElasticsearchFormRecordService elasticsearchFormRecordService(@Autowired ReactiveElasticsearchOperations operations,
                                                                         @Autowired DatacenterElasticsearchProperties properties) {
        return new ElasticsearchFormRecordService(operations, properties.getRecordPrefix());
    }

}
