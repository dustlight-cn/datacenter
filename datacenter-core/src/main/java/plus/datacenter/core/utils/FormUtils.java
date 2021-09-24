package plus.datacenter.core.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import plus.datacenter.core.entities.forms.Form;

import java.util.*;
import java.util.function.Function;

public class FormUtils {

    public static final String referenceFieldName = "form";
    public static final String propertiesFieldName = "properties";
    private static final String typeFieldName = "type";
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
            return Collections.emptyMap();
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
            else if (val instanceof ArrayNode) {
                ArrayNode arrayNode = (ArrayNode) val;
                Iterator<JsonNode> elem = arrayNode.elements();
                while (elem.hasNext()) {
                    searchReference(elem.next(), result, path);
                }
            }
        }
    }

    private static void searchFieldsByType(JsonNode root,
                                           Set<String> result,
                                           String path,
                                           String targetType,
                                           Function<JsonNode, Boolean> checker) {
        if (root == null || !root.fields().hasNext())
            return;
        Iterator<Map.Entry<String, JsonNode>> iter = root.fields();
        while (iter.hasNext()) {
            Map.Entry<String, JsonNode> kv = iter.next();
            String key = kv.getKey();
            JsonNode val = kv.getValue();
            if (typeFieldName.equals(key) && val instanceof TextNode) {
                Boolean checkResult;
                if (val.asText().equals(targetType) &&
                        (checker == null || (checkResult = checker.apply(root)) != null && checkResult)) {
                    result.add(path);
                }
            } else if (val instanceof ObjectNode)
                searchFieldsByType(val, result, path.length() > 0 ? path + "/" + key : key, targetType, checker);
        }
    }

    public static Set<String> getFieldsByType(JsonNode node,
                                              String targetType,
                                              Function<JsonNode, Boolean> checker) {
        if (node == null)
            return Collections.emptySet();
        Set<String> result = new HashSet<>();
        searchFieldsByType(node, result, "", targetType, checker);
        return result;
    }

    public static Set<String> getFieldsByType(JsonNode node,
                                              String targetType) {
        if (node == null)
            return Collections.emptySet();
        Set<String> result = new HashSet<>();
        searchFieldsByType(node, result, "", targetType, null);
        return result;
    }

    public static Set<String> getFieldsByType(Form form,
                                              String targetType,
                                              Function<JsonNode, Boolean> checker) {
        if (form == null)
            return Collections.emptySet();
        JsonNode node = transformMapToJsonNode(form.getSchema());
        return getFieldsByType(node.get(propertiesFieldName), targetType, checker);
    }

    public static Set<String> getFieldsByType(Form form,
                                              String targetType) {
        if (form == null)
            return Collections.emptySet();
        JsonNode node = transformMapToJsonNode(form.getSchema());
        return getFieldsByType(node.get(propertiesFieldName), targetType);
    }
}
