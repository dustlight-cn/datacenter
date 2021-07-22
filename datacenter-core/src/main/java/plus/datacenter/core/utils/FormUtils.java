package plus.datacenter.core.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.util.Json;
import plus.datacenter.core.entities.forms.Form;
import plus.datacenter.core.entities.forms.Item;
import plus.datacenter.core.entities.forms.ItemType;
import plus.datacenter.core.entities.forms.items.*;

import java.time.Instant;
import java.util.Map;

public class FormUtils {

    public static Form transformForm(Map<String, Object> form) {
        if (form == null)
            return null;
        try {
            ObjectMapper mapper = Json.mapper();
            return mapper.readValue(mapper.writeValueAsString(form), Form.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Item transformItem(Map<String, Object> item) {
        if (item == null)
            return null;
        try {
            Class<? extends Item> targetClass = item.get("type") == null ? Item.class : getItemClass(getItemType(item.get("type").toString()));
            ObjectMapper mapper = Json.mapper();
            return mapper.readValue(mapper.writeValueAsString(item), targetClass);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object transformItemValue(Object value, ItemType type) {
        if (value == null)
            return null;
        try {
            Class<?> targetClass = getItemValueClass(type);
            if (targetClass == value.getClass())
                return value;
            ObjectMapper mapper = Json.mapper();
            return mapper.readValue(mapper.writeValueAsString(value), targetClass);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Class<? extends Item> getItemClass(ItemType type) {
        switch (type) {
            case INT:
                return IntItem.class;
            case DOUBLE:
                return DoubleItem.class;
            case USER:
                return UserItem.class;
            case STRING:
                return StringItem.class;
            case FILE:
                return FileItem.class;
            case SELECT:
                return SelectItem.class;
            case FORM:
                return FormItem.class;
            case DATE:
                return DateItem.class;
            case BOOLEAN:
                return BooleanItem.class;
            case ELASTIC:
                return ElasticItem.class;
            default:
                return Item.class;
        }
    }

    public static Class<?> getItemValueClass(ItemType type) {
        switch (type) {
            case INT:
                return Integer.class;
            case DOUBLE:
                return Double.class;
            case SELECT:
                return SelectItem.Selected.class;
            case DATE:
                return Instant.class;
            case BOOLEAN:
                return Boolean.class;
            case ELASTIC:
                return ElasticItem.ElasticValue.class;
            case USER:
            case FORM:
            case FILE:
            case STRING:
            default:
                return String.class;
        }
    }

    public static ItemType getItemType(String type) {
        if (type == null)
            return ItemType.STRING;
        switch (type.toUpperCase()) {
            case "INT":
                return ItemType.INT;
            case "DOUBLE":
                return ItemType.DOUBLE;
            case "FILE":
                return ItemType.FILE;
            case "SELECT":
                return ItemType.SELECT;
            case "FORM":
                return ItemType.FORM;
            case "DATE":
                return ItemType.DATE;
            case "BOOLEAN":
                return ItemType.BOOLEAN;
            case "USER":
                return ItemType.USER;
            case "ELASTIC":
                return ItemType.ELASTIC;
            case "STRING":
            default:
                return ItemType.STRING;
        }
    }
}
