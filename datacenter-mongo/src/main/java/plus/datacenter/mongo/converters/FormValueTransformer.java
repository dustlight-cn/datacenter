package plus.datacenter.mongo.converters;

import org.bson.types.ObjectId;
import plus.datacenter.core.entities.forms.ItemType;
import plus.datacenter.core.services.ItemValueTransformer;

/**
 * Form 类型值转换器
 * <p>
 * 将十六进制的字符串型的 ObjectID 转换成 ObjectID 对象。
 */
public class FormValueTransformer implements ItemValueTransformer {

    @Override
    public ItemType getItemType() {
        return ItemType.FORM;
    }

    @Override
    public Object transform(Object originValue) {
        return originValue instanceof ObjectId ? originValue : new ObjectId(String.valueOf(originValue));
    }
}
