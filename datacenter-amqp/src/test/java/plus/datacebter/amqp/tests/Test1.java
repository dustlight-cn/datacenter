package plus.datacebter.amqp.tests;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import plus.datacenter.amqp.AmqpEventHandler;

@SpringBootTest
public class Test1 {

    @Autowired
    private AmqpEventHandler amqpEventHandler;

    private static final Log logger = LogFactory.getLog(Test1.class);

    @Test
    public void test() {
        logger.info(amqpEventHandler.getTemplate().getExchange());
    }
}
