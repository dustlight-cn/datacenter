package plus.datacenter.jsonschema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.net.URI;
import java.util.Set;


@SpringBootTest
public class Test1 {

    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void test1() throws Throwable {
        ClassPathResource fromSchemaResource = new ClassPathResource("form.json");
        ClassPathResource dataResource = new ClassPathResource("data.json");


        JsonSchema schema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V6).getSchema(new URI("http://localhost:8080/v1/schemas/form"));

        JsonNode node = mapper.readValue(fromSchemaResource.getInputStream(), JsonNode.class);

        ValidationResult result = schema.validateAndCollect(node);

        if (result.getValidationMessages().size() != 0) {
            StringBuilder stringBuilder = new StringBuilder();
            for (ValidationMessage message : result.getValidationMessages()) {
                stringBuilder.append(message.getMessage());
                stringBuilder.append("\n");
            }
            throw new RuntimeException(stringBuilder.toString());
        } else {
            System.out.println(mapper.writeValueAsString(node));
        }

        JsonSchema formschema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V6).getSchema(node);

        JsonNode data = mapper.readValue(dataResource.getInputStream(), JsonNode.class);
        Set<ValidationMessage> msgs = formschema.validate(data);
        if (msgs.size() > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            for (ValidationMessage message : msgs) {
                stringBuilder.append(message.getMessage());
                stringBuilder.append("\n");
            }
            throw new RuntimeException(stringBuilder.toString());
        }else{
            System.out.println(mapper.writeValueAsString(data));
        }
    }
}
