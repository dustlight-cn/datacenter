package cn.dustlight.datacenter.schema.adapter;

import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import cn.dustlight.datacenter.schema.Schemas;

@Configuration
@EnableConfigurationProperties(SchemaAdapterProperties.class)
public class SchemaAdapterConfiguration {

    @Bean
    public JsonSchemaFactory jsonSchemaFactory(@Autowired Schemas schemas,
                                               @Autowired SchemaAdapterProperties properties) {
        return AdapterFactory.get(schemas, properties.getMetaSchema(), properties.getNonValidationKeywords());
    }

    @Bean
    public JsonSchema formSchema(@Autowired JsonSchemaFactory factory,
                                 @Autowired Schemas schemas,
                                 @Autowired SchemaAdapterProperties properties) {
        Schemas.Schema f = schemas.getSchemaMap().get(properties.getFormSchema());
        return factory.getSchema(f.getJsonNode());
    }
}
