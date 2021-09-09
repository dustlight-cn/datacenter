package plus.datacenter.core.services;

import plus.datacenter.core.entities.forms.ItemType;

/**
 * 表单项值转换器
 */
public interface ItemValueTransformer {

    /**
     * 目标类型
     *
     * @return
     */
    ItemType getItemType();

    /**
     * 值转换
     *
     * @param originValue
     * @return
     */
    Object transform(Object originValue);
}
