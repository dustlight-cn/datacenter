package plus.datacenter.schema.adapter;

import com.networknt.schema.JsonSchemaFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import plus.datacenter.schema.Schemas;

@Configuration
@EnableConfigurationProperties(SchemaAdapterProperties.class)
public class SchemaAdapterConfiguration {

    @Bean
    @ConditionalOnBean(Schemas.class)
    public JsonSchemaFactory jsonSchemaFactory(@Autowired Schemas schemas,
                                               @Autowired SchemaAdapterProperties properties) {
        return AdapterFactory.get(schemas, properties.getMetaKey(), properties.getNonValidationKeywords());
    }

}
