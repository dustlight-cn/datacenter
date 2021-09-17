package plus.datacenter.schema.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import plus.datacenter.schema.Schemas;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

@Configuration
@Import(SchemaConfiguration.class)
public class SchemaResourcesConfiguration implements WebFluxConfigurer {

    @Autowired
    private Schemas schemas;

    @Autowired
    private SchemaResourceProperties properties;

    private final static ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        File dir = new File(properties.getOutputDir());

        if ((!dir.exists() || !dir.isDirectory()) && !dir.mkdirs())
            throw new RuntimeException(String.format("Fail to make dir '%s'", dir.getAbsoluteFile()));
        Map<String, Schemas.Schema> map = schemas.getSchemaMap();
        for (var kv : map.entrySet()) {
            writeSchema(dir, kv.getKey(), kv.getValue());
        }

        if (map.size() > 0)
            registry.addResourceHandler(properties.getMapping())
                    .addResourceLocations(dir.toURI().toASCIIString())
                    .setUseLastModified(true);
    }

    private static void writeSchema(File dir, String name, Schemas.Schema schema) throws IOException {
        File file = new File(dir, name);
        File parent = file.getParentFile();
        if ((!parent.exists() || !parent.isDirectory()) && !parent.mkdirs())
            throw new RuntimeException(String.format("Fail to make dir '%s'", parent.getAbsoluteFile()));
        FileOutputStream outputStream = new FileOutputStream(file);
        objectMapper.writeValue(outputStream, schema.getJsonNode());
        outputStream.close();
    }
}
