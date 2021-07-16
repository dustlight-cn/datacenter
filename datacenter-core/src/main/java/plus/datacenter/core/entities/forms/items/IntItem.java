package plus.datacenter.core.entities.forms.items;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import plus.datacenter.core.entities.Rangeable;
import plus.datacenter.core.entities.forms.Item;
import plus.datacenter.core.entities.forms.ItemType;

@Getter
@Setter
public class IntItem extends Item {

    private Rangeable<Integer> range;

    @Schema(defaultValue = "INT")
    @Override
    public ItemType getType() {
        return ItemType.INT;
    }

    @Override
    public void setType(ItemType type) {
        super.setType(ItemType.INT);
    }
}
