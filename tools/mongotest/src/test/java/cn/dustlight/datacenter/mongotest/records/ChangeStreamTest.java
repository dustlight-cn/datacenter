package cn.dustlight.datacenter.mongotest.records;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import java.util.Map;

@SpringBootTest
public class ChangeStreamTest {


    @Autowired
    ReactiveMongoTemplate reactiveMongoTemplate;

    @Test
    public void test() {
        Log logger = LogFactory.getLog(getClass());
        Gson gson = new Gson();

        reactiveMongoTemplate.changeStream(Map.class)
                .watchCollection("form_record")
                .listen()
                .map(mapChangeStreamEvent -> {
                    ChangeStreamEvent<Map> x = mapChangeStreamEvent;
                    logger.info(gson.toJson(mapChangeStreamEvent.getResumeToken()));
                    logger.info(gson.toJson(mapChangeStreamEvent.getBody()));
                    return mapChangeStreamEvent;
                })
                .blockLast();
    }
}
