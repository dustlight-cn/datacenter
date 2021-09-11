package plus.datacenter.application.configurations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import plus.datacenter.core.services.DefaultRecordValidator;
import plus.datacenter.core.services.FormService;
import plus.datacenter.core.services.ItemValueTransformer;

@Configuration
public class RecordValidatorConfiguration {

    @Bean
    public DefaultRecordValidator defaultRecordValidator(@Autowired FormService formService,
                                                         @Autowired ApplicationContext context) {
        DefaultRecordValidator validator = new DefaultRecordValidator(formService);
        validator.setTransformers(context.getBeansOfType(ItemValueTransformer.class).values());
        return validator;
    }
}
