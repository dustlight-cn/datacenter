package plus.datacenter.application.configurations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import plus.datacenter.application.services.DefaultPrincipalHolder;
import plus.datacenter.application.services.ElasticsearchSyncHandler;
import plus.datacenter.core.services.EnhancedRecordService;
import plus.datacenter.core.services.PrincipalHolder;

@Configuration
public class ServiceConfig {

    @Bean
    @ConditionalOnMissingBean(PrincipalHolder.class)
    public DefaultPrincipalHolder defaultPrincipalHolder() {
        return new DefaultPrincipalHolder();
    }

    @Bean
    public ElasticsearchSyncHandler elasticsearchSyncHandler(@Autowired EnhancedRecordService recordService) {
        return new ElasticsearchSyncHandler(recordService);
    }
}
