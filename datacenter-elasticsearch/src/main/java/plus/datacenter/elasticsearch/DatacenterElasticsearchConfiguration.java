package plus.datacenter.elasticsearch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchCustomConversions;
import org.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverter;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import plus.datacenter.elasticsearch.converters.LongToInstantConverter;
import plus.datacenter.elasticsearch.services.ElasticsearchFormRecordSearcher;
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

    @Bean
    @ConditionalOnBean(ReactiveElasticsearchOperations.class)
    public ElasticsearchFormRecordSearcher elasticsearchFormRecordSearcher(@Autowired ReactiveElasticsearchOperations operations,
                                                                           @Autowired DatacenterElasticsearchProperties properties) {
        return new ElasticsearchFormRecordSearcher(operations, properties.getRecordPrefix());
    }

    @Bean
    @Primary
    public ElasticsearchConverter dcElasticsearchConverter(SimpleElasticsearchMappingContext mappingContext,
                                                         ElasticsearchCustomConversions elasticsearchCustomConversions) {
        DefaultConversionService conversionService = new DefaultConversionService();
        conversionService.addConverter(new LongToInstantConverter());
        MappingElasticsearchConverter converter = new MappingElasticsearchConverter(mappingContext, conversionService);
        converter.setConversions(elasticsearchCustomConversions);
        return converter;
    }
}
