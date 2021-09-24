package plus.datacenter.application.configurations;

import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import plus.datacenter.core.services.FormService;
import plus.datacenter.core.services.ItemValueTransformer;
import plus.datacenter.schema.adapter.JsonSchemaFormValidator;
import plus.datacenter.schema.adapter.JsonSchemaRecordValidator;

@Configuration
public class ValidatorConfiguration {

    @Bean
    public JsonSchemaRecordValidator jsonSchemaRecordValidator(@Autowired FormService formService,
                                                               @Autowired JsonSchemaFactory factory,
                                                               @Autowired JsonSchema formSchema,
                                                               @Autowired ApplicationContext context) {
        JsonSchemaRecordValidator validator = new JsonSchemaRecordValidator(formService, factory);
        validator.setFormSchemaId(formSchema.getCurrentUri().toASCIIString() + "_INSTANCE");
        validator.setTransformers(context.getBeansOfType(ItemValueTransformer.class).values());
        return validator;
    }

    @Bean
    public JsonSchemaFormValidator jsonSchemaFormValidator(@Autowired JsonSchema formSchema) {
        JsonSchemaFormValidator validator = new JsonSchemaFormValidator(formSchema);
        return validator;
    }
}
