package plus.datacenter.schema.resources;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "plus.datacenter.schema")
public class SchemaResourceProperties {

    private String mapping = "schemas/**";
    private String templatePath = "schema-templates";
    private String outputDir = "schemas";

    private Map<String, String> parameters = new HashMap<>();

}
