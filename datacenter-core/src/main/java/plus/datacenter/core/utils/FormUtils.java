package plus.datacenter.core.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import plus.datacenter.core.entities.forms.Form;

import java.util.*;

public class FormUtils {

    public static final String referenceFieldName = "form";
    public static final String propertiesFieldName = "properties";
    public static final ObjectMapper mapper = new ObjectMapper();

    public static JsonNode transformMapToJsonNode(Map map) {
        if (map == null)
            return null;
        return mapper.convertValue(map, JsonNode.class);
    }

    public static void fillReference(Form form) {
        if (form == null || form.getSchema() == null)
            return;
        Map<String, String> map = getReference(form);
        if (map == null || map.size() == 0) {
            form.setReferences(null);
            form.setReferenceMap(null);
            return;
        }
        Set<String> references = new HashSet<>(map.values());
        form.setReferences(references);
        form.setReferenceMap(map);
    }

    public static Map<String, String> getReference(Form form) {
        if (form == null)
            Collections.emptySet();
        JsonNode node = transformMapToJsonNode(form.getSchema());
        return getReference(node.get(propertiesFieldName));
    }

    public static Map<String, String> getReference(JsonNode node) {
        if (node == null)
            return Collections.emptyMap();
        Map<String, String> result = new HashMap<>();
        searchReference(node, result, "");
        return result;
    }

    private static void searchReference(JsonNode root, Map<String, String> result, String path) {
        if (root == null || !root.fields().hasNext())
            return;
        Iterator<Map.Entry<String, JsonNode>> iter = root.fields();
        while (iter.hasNext()) {
            Map.Entry<String, JsonNode> kv = iter.next();
            String key = kv.getKey();
            JsonNode val = kv.getValue();
            if (referenceFieldName.equals(key) && val instanceof TextNode)
                result.put(path, val.asText());
            else if (val instanceof ObjectNode)
                searchReference(val, result, path.length() > 0 ? path + "/" + key : key);
        }
    }
}
