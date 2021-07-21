package plus.datacenter.mongo;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "plus.datacenter.mongo")
public class DatacenterMongoProperties {

    private String formCollection = "form";

    private String formRecordCollection = "form_record";

    private boolean enqueue = true;
    private String exchange = "datacenter";

}
