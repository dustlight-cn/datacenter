package plus.datacenter.schema.adapter;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "plus.datacenter.schema.adapter")
public class SchemaAdapterProperties {

    private String metaKey;
    private String[] nonValidationKeywords;
}
