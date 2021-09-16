package plus.datacenter.schema.resources;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "plus.datacenter.schema")
public class SchemaResourceProperties {

    private String prefix = "";
    private String templateResourcePath = "schemas";
    private String outputDir = "schemas";

}
