package plus.datacenter.core.utils;

import plus.datacenter.core.entities.forms.Item;
import plus.datacenter.core.entities.forms.ItemType;
import plus.datacenter.core.entities.forms.items.*;

import java.util.Collection;
import java.util.Date;

public class FormUtils {

    public static Class<? extends Item> getItemClass(ItemType type) {
        switch (type) {
            case INT:
                return IntItem.class;
            case DOUBLE:
                return DoubleItem.class;
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
            case STRING:
                return String.class;
            case FILE:
                return String.class;
            case SELECT:
                return Collection.class;
            case FORM:
                return String.class;
            case DATE:
                return Date.class;
            case BOOLEAN:
                return Boolean.class;
            default:
                return String.class;
        }
    }
}
