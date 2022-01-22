package cn.dustlight.datacenter.elasticsearch;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "plus.datacenter.elasticsearch")
public class DatacenterElasticsearchProperties {

    private String formPrefix = "datacenter.form";
    private String recordPrefix = "datacenter.form_record";
}
