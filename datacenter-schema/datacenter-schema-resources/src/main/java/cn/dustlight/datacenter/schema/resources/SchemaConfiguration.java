package cn.dustlight.datacenter.schema.resources;

import cn.dustlight.datacenter.schema.Schemas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@EnableConfigurationProperties(SchemaResourceProperties.class)
public class SchemaConfiguration {

    @Bean
    public Schemas schemas(@Autowired SchemaResourceProperties properties) throws IOException {
        return Schemas.get(properties.getTemplatePath(), properties.getParameters());
    }
}
