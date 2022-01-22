package cn.dustlight.datacenter.amqptest.records;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AmqpTest {

    @Test
    public void test(){
        Object o = new ObjectId();
    }

}
