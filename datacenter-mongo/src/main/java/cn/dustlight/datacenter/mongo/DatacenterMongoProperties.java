package cn.dustlight.datacenter.mongo;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "dustlight.datacenter.mongo")
public class DatacenterMongoProperties {

    /**
     * 表单集合的名称
     */
    private String formCollection = "form";

    /**
     * 记录集合的名称
     */
    private String recordCollection = "form_record";

    /**
     * 是否自动注入 Handler
     */
    private boolean autoInjectHandlers = true;

    /**
     * 是否自动注入验证器
     */
    private boolean autoInjectValidators = true;

}
