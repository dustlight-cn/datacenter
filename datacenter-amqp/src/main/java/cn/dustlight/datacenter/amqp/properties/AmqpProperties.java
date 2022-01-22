package cn.dustlight.datacenter.amqp.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "dustlight.datacenter.amqp")
public class AmqpProperties {

    private String exchange = "datacenter";

    private int order = 0;

    private SyncProperties sync = new SyncProperties();

}
