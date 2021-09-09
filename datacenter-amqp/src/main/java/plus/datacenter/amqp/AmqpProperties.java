package plus.datacenter.amqp;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "plus.datacenter.amqp")
public class AmqpProperties {

    private String exchange = "datacenter";

    private int order = 0;
}
