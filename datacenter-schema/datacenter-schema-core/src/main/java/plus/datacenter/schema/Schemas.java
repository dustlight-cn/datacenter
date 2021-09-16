package plus.datacenter.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Schemas {

    private String prefix;
    private ObjectMapper mapper;
    private Map<String, Schema> schemaMap;
    private Map<String, String> parameters;

    public static Schemas get(String prefix, String templateResourcePath) throws IOException {
        return new Schemas(prefix, templateResourcePath);
    }

    public static Schemas get(String prefix) throws IOException {
        return get(prefix, "schemas");
    }

    private Schemas(String prefix, String templateResourcePath) throws IOException {
        this.prefix = prefix;
        this.parameters = new HashMap<>();
        this.parameters.put("prefix", prefix);
        this.mapper = new ObjectMapper();
        this.schemaMap = new HashMap<>();
        ClassPathResource dir = new ClassPathResource(templateResourcePath);
        searchSchemas(dir.getFile(), "", schemaMap, 0);
    }

    private void searchSchemas(File file, String prefix, Map<String, Schema> fileMap, int deep) throws IOException {
        if (file == null)
            return;
        String key = deep == 0 ? prefix : String.format("%s/%s", prefix, file.getName());
        if (file.isFile())
            fileMap.put(key, loadJsonNode(file, key));
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                searchSchemas(f, key, fileMap, deep + 1);
            }
        }
    }

    private Schema loadJsonNode(File file, String key) throws IOException {
        file.lastModified();
        String json = readFileAsString(file, parameters);
        JsonNode node = this.mapper.readValue(json, JsonNode.class);
        return new Schema(json, node);
    }

    private static String readFileAsString(File file, Map<String, String> replace) throws IOException {
        FileInputStream in = new FileInputStream(file);
        Long len = file.length();
        byte[] content = new byte[len.intValue()];
        in.read(content);
        in.close();
        String json = new String(content);
        if (replace != null) {
            Iterator<Map.Entry<String, String>> iter = replace.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, String> kv = iter.next();
                json = json.replace(String.format("${%s}", kv.getKey()), kv.getValue());
            }
        }
        return json;
    }

    public Map<String, Schema> getSchemaMap() {
        return schemaMap;
    }

    public String getPrefix() {
        return prefix;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Schema {
        private String json;
        private JsonNode jsonNode;
    }
}
