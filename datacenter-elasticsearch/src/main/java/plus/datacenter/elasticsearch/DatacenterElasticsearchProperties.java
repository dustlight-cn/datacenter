package plus.datacenter.elasticsearch;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "plus.datacenter.elasticsearch")
public class DatacenterElasticsearchProperties {

    private String recordPrefix = "datacenter.form_record";
}
