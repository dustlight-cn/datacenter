package cn.dustlight.datacenter.schema.adapter;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "dustlight.datacenter.schema.adapter")
public class SchemaAdapterProperties {

    private String metaSchema;
    private String formSchema;
    private String[] nonValidationKeywords;
}
