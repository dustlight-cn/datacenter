package plus.datacenter.schema.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.util.Set;

@SpringBootTest
public class SchemaTest {

    @Autowired
    JsonSchemaFactory factory;

    @Autowired
    JsonSchema formSchema;

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @SneakyThrows
    public void test() {
        JsonNode formNode = objectMapper.readValue(new ClassPathResource("form.json").getInputStream(), JsonNode.class);
        JsonNode dataNode = objectMapper.readValue(new ClassPathResource("data.json").getInputStream(), JsonNode.class);

        check(formSchema.validate(formNode));

        JsonSchema formSchema = factory.getSchema(formNode);

        check(formSchema.validate(dataNode));
    }

    private static void check(Set<ValidationMessage> messageSet) {

        if (messageSet != null && messageSet.size() > 0) {
            StringBuilder builder = new StringBuilder();
            for (ValidationMessage message : messageSet) {
                builder.append(message);
                builder.append('\n');
            }
            throw new RuntimeException("Validation Failed: \n" + builder.toString());
        }
    }
}
