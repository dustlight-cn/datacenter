package plus.datacenter.schema.adapter;

import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import plus.datacenter.schema.Schemas;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@Configuration
public class SchemaTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchemaTestApplication.class, args);
    }

    @SneakyThrows
    @Bean
    public Schemas schemas() {
        String templatePath = "../../schema-templates";
        Map<String, String> params = new HashMap<>();
        params.put("endpoint", "http://localhost:8080");
        params.put("prefix", "v1/schemas");
        return Schemas.get(templatePath, params);
    }
}
