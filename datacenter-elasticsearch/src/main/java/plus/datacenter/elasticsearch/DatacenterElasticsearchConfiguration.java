package plus.datacenter.elasticsearch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import plus.datacenter.elasticsearch.services.ElasticsearchFormSearcher;

import java.time.Instant;
import java.util.Date;

@Configuration
public class DatacenterElasticsearchConfiguration {

    @Bean
    @ConditionalOnBean(ReactiveElasticsearchOperations.class)
    public ElasticsearchFormSearcher elasticsearchFormSearcher(@Autowired ReactiveElasticsearchOperations operations) {
        return new ElasticsearchFormSearcher(operations);
    }

    @Bean
    public StringDateConverter stringDateConverter() {
        return new StringDateConverter();
    }

    @ReadingConverter
    public static class StringDateConverter implements Converter<String, Date> {

        @Override
        public Date convert(String source) {
            return Date.from(Instant.parse(source));
        }
    }
}
