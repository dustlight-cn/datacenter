package cn.dustlight.datacenter.application.configurations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import cn.dustlight.datacenter.application.services.DefaultPrincipalHolder;
import cn.dustlight.datacenter.application.services.ElasticsearchSyncHandler;
import cn.dustlight.datacenter.core.services.EnhancedRecordService;
import cn.dustlight.datacenter.core.services.PrincipalHolder;
import cn.dustlight.datacenter.elasticsearch.services.ElasticsearchRecordService;

@Configuration
public class ServiceConfig {

    @Bean
    @ConditionalOnMissingBean(PrincipalHolder.class)
    public DefaultPrincipalHolder defaultPrincipalHolder() {
        return new DefaultPrincipalHolder();
    }

    @Bean
    public ElasticsearchSyncHandler elasticsearchSyncHandler(@Autowired EnhancedRecordService recordService,
                                                             @Autowired ElasticsearchRecordService elasticsearchRecordService) {
        return new ElasticsearchSyncHandler(recordService, elasticsearchRecordService);
    }
}
