package plus.datacenter.test;

import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.Date;


public class Test {

    @org.junit.jupiter.api.Test
    public void test1() {
        Date date = Date.from(Instant.parse("2021-07-19T06:01:12.133Z"));
        System.out.println(date);
    }
}
