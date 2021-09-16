package plus.datacenter.schema.resources;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import plus.datacenter.schema.Schemas;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(SchemaResourceProperties.class)
public class SchemaResourcesConfiguration implements WebFluxConfigurer {

    @Autowired
    private SchemaResourceProperties properties;

    @SneakyThrows
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        URI uri = URI.create(properties.getPrefix());
        Schemas schemas = Schemas.get(properties.getPrefix(), properties.getTemplateResourcePath());
        File dir = new File(properties.getOutputDir());
        if ((!dir.exists() || !dir.isDirectory()) && !dir.mkdirs())
            throw new RuntimeException(String.format("Fail to make dir '%s'", dir.getAbsoluteFile()));
        Map<String, Schemas.Schema> map = schemas.getSchemaMap();
        for (var kv : map.entrySet()) {
            writeSchema(dir, kv.getKey(), kv.getValue());
        }
        registry.addResourceHandler(uri.getPath() + "/**")
                .addResourceLocations(dir.toURI().toASCIIString())
                .setUseLastModified(true);
    }

    private static void writeSchema(File dir, String name, Schemas.Schema schema) throws IOException {
        File file = new File(dir, name);
        File parent = file.getParentFile();
        if ((!parent.exists() || !parent.isDirectory()) && !parent.mkdirs())
            throw new RuntimeException(String.format("Fail to make dir '%s'", parent.getAbsoluteFile()));
        FileOutputStream outputStream = new FileOutputStream(file);
        outputStream.write(schema.getJson().getBytes(StandardCharsets.UTF_8));
        outputStream.close();
    }
}
