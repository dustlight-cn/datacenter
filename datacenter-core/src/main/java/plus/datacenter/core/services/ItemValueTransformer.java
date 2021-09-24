package plus.datacenter.core.services;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 表单项值转换器
 */
public interface ItemValueTransformer {

    /**
     * 检查是否需要转换
     *
     * @param schema
     * @return
     */
    boolean check(JsonNode schema);

    /**
     * 值转换
     *
     * @param originValue
     * @return
     */
    Object transform(Object originValue);
}
