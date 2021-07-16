package plus.datacenter.core.entities.forms.items;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import plus.datacenter.core.entities.forms.Item;
import plus.datacenter.core.entities.forms.ItemType;

@Getter
@Setter
public class BooleanItem extends Item {

    @Schema(defaultValue = "BOOLEAN")
    @Override
    public ItemType getType() {
        return ItemType.BOOLEAN;
    }

    @Override
    public void setType(ItemType type) {
        super.setType(ItemType.BOOLEAN);
    }
}
