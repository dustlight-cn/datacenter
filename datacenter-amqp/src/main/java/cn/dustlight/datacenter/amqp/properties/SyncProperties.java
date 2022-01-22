package cn.dustlight.datacenter.amqp.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "plus.datacenter.amqp.sync")
public class SyncProperties {

    private boolean enabled = true;
    private String queue = "datacenter-sync";
    private String deadLetterQueue = "datacenter-sync-dead";
    private String deadLetterRoutingKey = "SYNC-FAIL";
}
