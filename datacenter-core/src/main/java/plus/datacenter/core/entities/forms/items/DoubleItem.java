package plus.datacenter.core.entities.forms.items;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import plus.datacenter.core.entities.Rangeable;
import plus.datacenter.core.entities.forms.Item;
import plus.datacenter.core.entities.forms.ItemType;

@Getter
@Setter
public class DoubleItem extends Item<Double> {

    private Rangeable<Double> range;

    @Override
    public Boolean validate(Double value) {
        return super.validate(value) && (range == null || range.validate(value));
    }

    @Schema(defaultValue = "DOUBLE")
    @Override
    public ItemType getType() {
        return ItemType.DOUBLE;
    }

    @Override
    public void setType(ItemType type) {
        super.setType(ItemType.DOUBLE);
    }
}
