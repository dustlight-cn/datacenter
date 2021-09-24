package plus.datacenter.mongo.converters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.bson.types.ObjectId;
import plus.datacenter.core.services.ItemValueTransformer;

import java.util.Iterator;

/**
 * Form 类型值转换器
 * <p>
 * 将十六进制的字符串型的 ObjectID 转换成 ObjectID 对象。
 */
public class FormValueTransformer implements ItemValueTransformer {

    @Override
    public boolean check(JsonNode schema) {
        if (schema == null)
            return false;
        if (schema.has("$ref"))
            return true;
        Iterator<JsonNode> iter = schema.elements();
        while (iter.hasNext()) {
            JsonNode node = iter.next();
            if (node instanceof ArrayNode) {
                Iterator<JsonNode> iter2 = node.elements();
                while (iter2.hasNext()) {
                    if (check(iter2.next()))
                        return true;
                }
            }
        }
        return false;
    }

    @Override
    public Object transform(Object originValue) {
        return (originValue instanceof ObjectId || !(originValue instanceof String)) ?
                originValue :
                new ObjectId((String) originValue);
    }
}
