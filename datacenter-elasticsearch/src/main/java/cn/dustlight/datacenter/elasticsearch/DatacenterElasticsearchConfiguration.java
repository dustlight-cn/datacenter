package cn.dustlight.datacenter.elasticsearch;

import cn.dustlight.datacenter.elasticsearch.converters.InstantToStringConverter;
import cn.dustlight.datacenter.elasticsearch.converters.ObjectIdToStringConverter;
import cn.dustlight.datacenter.elasticsearch.services.ElasticsearchFormSearcher;
import cn.dustlight.datacenter.elasticsearch.services.ElasticsearchRecordSearcher;
import cn.dustlight.datacenter.elasticsearch.services.ElasticsearchRecordService;
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

import java.util.Arrays;

@EnableConfigurationProperties(DatacenterElasticsearchProperties.class)
@Configuration
public class DatacenterElasticsearchConfiguration {

    @Bean
    @ConditionalOnBean(ReactiveElasticsearchOperations.class)
    public ElasticsearchFormSearcher elasticsearchFormSearcher(@Autowired ReactiveElasticsearchOperations operations,
                                                               @Autowired DatacenterElasticsearchProperties properties) {
        return new ElasticsearchFormSearcher(operations, properties.getFormPrefix());
    }

    @Bean
    @ConditionalOnBean(ReactiveElasticsearchOperations.class)
    public ElasticsearchRecordSearcher elasticsearchFormRecordSearcher(@Autowired ReactiveElasticsearchOperations operations,
                                                                       @Autowired DatacenterElasticsearchProperties properties) {
        return new ElasticsearchRecordSearcher(operations, properties.getRecordPrefix());
    }

    @Bean
    @ConditionalOnBean(ReactiveElasticsearchOperations.class)
    public ElasticsearchRecordService elasticsearchRecordService(@Autowired ReactiveElasticsearchOperations operations,
                                                                 @Autowired DatacenterElasticsearchProperties properties) {
        return new ElasticsearchRecordService(operations, properties.getRecordPrefix());
    }

    @Bean
    @Primary
    public ElasticsearchConverter dcElasticsearchConverter(SimpleElasticsearchMappingContext mappingContext) {
        DefaultConversionService conversionService = new DefaultConversionService();
        conversionService.addConverter(new InstantToStringConverter());
        conversionService.addConverter(new ObjectIdToStringConverter());
        MappingElasticsearchConverter converter = new MappingElasticsearchConverter(mappingContext, conversionService);
        converter.setConversions(new ElasticsearchCustomConversions(Arrays.asList(new InstantToStringConverter(),
                new ObjectIdToStringConverter())));
        return converter;
    }

}
