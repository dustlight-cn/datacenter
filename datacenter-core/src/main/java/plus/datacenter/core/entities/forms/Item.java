package plus.datacenter.core.entities.forms;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;
import plus.datacenter.core.entities.Validatable;
import plus.datacenter.core.entities.forms.items.*;
import plus.datacenter.core.utils.ItemDeserializer;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@Schema(oneOf = {
        BooleanItem.class,
        DateItem.class,
        DoubleItem.class,
        FileItem.class,
        FormItem.class,
        IntItem.class,
        SelectItem.class,
        StringItem.class,
        UserItem.class
})
@Getter
@Setter
@JsonDeserialize(using = ItemDeserializer.class)
public class Item<T> implements Serializable, Validatable<T> {

    private String name;
    private String label;
    private String description;
    private ItemType type;
    private Boolean array;
    private Boolean required;

    @Override
    public Boolean validate(T value) {
        if (required == null || required == false)
            return true;
        if (value == null)
            return false;
        if (value instanceof Collection)
            return ((Collection<?>) value).size() > 0;
        if (value instanceof List)
            return ((Collection<?>) value).size() > 0;
        if (value instanceof String)
            return StringUtils.hasText((String) value);
        return true;
    }
}
