package plus.datacenter.mongo;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "plus.datacenter.mongo")
public class DatacenterMongoProperties {

    /**
     * 表单集合的名称
     */
    private String formCollection = "form";

    /**
     * 记录集合的名称
     */
    private String recordCollection = "form_record";

}
