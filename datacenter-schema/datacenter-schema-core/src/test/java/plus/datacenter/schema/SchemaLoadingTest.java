package plus.datacenter.schema;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.Map;

@RestController
@SpringBootTest
public class SchemaLoadingTest {

    @Test
    public void test() throws IOException {
        Schemas schemas = Schemas.get("http://examples.com");
        Map<String, Schemas.Schema> sm = schemas.getSchemaMap();

        for (var x : sm.entrySet()) {
            String json = x.getValue().getJson();
            System.out.println(json);
        }
    }
}
