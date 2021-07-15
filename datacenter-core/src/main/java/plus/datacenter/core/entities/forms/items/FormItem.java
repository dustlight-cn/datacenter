package plus.datacenter.core.entities.forms.items;

import lombok.Getter;
import lombok.Setter;
import plus.datacenter.core.entities.forms.Item;

@Getter
@Setter
public class FormItem extends Item {

    /**
     * 当 type 为 FORM 时，对应的表单 id。
     */
    private String formId;

}
