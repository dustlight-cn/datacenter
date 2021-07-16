package plus.datacenter.core.entities.forms;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import plus.datacenter.core.entities.forms.items.*;

import java.io.Serializable;

@Schema(oneOf = {
        BooleanItem.class,
        DateItem.class,
        DoubleItem.class,
        FileItem.class,
        FormItem.class,
        IntItem.class,
        SelectItem.class,
        StringItem.class
})
@Getter
@Setter
public class Item implements Serializable {

    private String name;
    private String label;
    private String description;
    private ItemType type;
    private Boolean array;

}
