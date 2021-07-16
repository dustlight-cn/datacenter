package plus.datacenter.core.entities.forms.items;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import plus.datacenter.core.entities.forms.Item;
import plus.datacenter.core.entities.forms.ItemType;

@Getter
@Setter
public class FormItem extends Item {

    /**
     * 当 type 为 FORM 时，对应的表单 id。
     */
    private String formId;

    @Schema(defaultValue = "FORM")
    @Override
    public ItemType getType() {
        return ItemType.FORM;
    }

    @Override
    public void setType(ItemType type) {
        super.setType(ItemType.FORM);
    }
}
