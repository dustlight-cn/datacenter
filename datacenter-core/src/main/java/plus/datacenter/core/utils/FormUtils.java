package plus.datacenter.core.utils;

import plus.datacenter.core.entities.forms.Item;
import plus.datacenter.core.entities.forms.ItemType;
import plus.datacenter.core.entities.forms.items.*;

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
}
