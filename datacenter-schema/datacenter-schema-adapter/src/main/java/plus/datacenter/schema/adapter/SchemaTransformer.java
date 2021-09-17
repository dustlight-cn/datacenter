package plus.datacenter.schema.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import plus.datacenter.core.entities.forms.Item;
import plus.datacenter.core.entities.forms.ItemGroup;
import plus.datacenter.core.entities.forms.items.ElasticItem;
import plus.datacenter.core.entities.forms.items.FormItem;
import plus.datacenter.core.entities.forms.items.IntItem;

import java.util.*;

@Getter
@Setter
@AllArgsConstructor
public class SchemaTransformer {

    private JsonSchemaFactory factory;

    private JsonSchema schema;

    public ItemGroup transformItems(JsonNode node, boolean validate) {
        if (validate)
            validate(node, schema);
        ItemGroup group = new ItemGroup();
        group.setName(node.get("title").asText());
        group.setDescription(node.get("description").asText());
        // required?
        if (node.has("properties")) {
            JsonNode properties = node.get("properties");
            Iterator<Map.Entry<String, JsonNode>> iter = properties.fields();
            Collection<Item> items = new HashSet<>();
            while (iter.hasNext()) {
                Map.Entry<String, JsonNode> kv = iter.next();
                items.add(transformItem(kv.getKey(), kv.getValue(), false));
            }
            group.setItems(items);
        }
        return group;
    }

    public Item transformItem(String name, JsonNode node, boolean required) {
        Item result = null;
        if (node.has("type")) {
            String type = node.get("type").asText();
            switch (type) {
                case "array":
                    Item item = transformItem(name,node.get("items"),required);
                    item.setArray(true);
                    break;
                case "integer":
                    IntItem intItem = new IntItem();
                    node.has("")
                    intItem.set
            }
        } else if (node.has("$form")) {
            FormItem item = new FormItem();
            item.setForm(node.get("$form").asText());
            result = item;
        }

        if (result == null) {
            throw new RuntimeException("Unsupported format");
        }

        result.setName(name);
        if (node.has("title"))
            result.setLabel(node.get("title").asText());
        if (node.has("description"))
            result.setLabel(node.get("description").asText());
        result.setRequired(required);
        return result;
    }

    protected void validate(JsonNode node, JsonSchema schema) {
        if (node == null)
            throw new NullPointerException("Node can not be null");
        if (schema == null)
            throw new NullPointerException("Schema can not be null");
        Set<ValidationMessage> result = schema.validate(node);
        if (result != null && result.size() > 0) {
            StringBuilder builder = new StringBuilder();
            for (ValidationMessage message : result) {
                if (builder.length() > 0)
                    builder.append('\n');
                builder.append(message.getMessage());
            }
            throw new RuntimeException(builder.toString());
        }
    }
}
