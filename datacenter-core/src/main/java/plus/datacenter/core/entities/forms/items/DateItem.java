package plus.datacenter.core.entities.forms.items;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import plus.datacenter.core.entities.Rangeable;
import plus.datacenter.core.entities.forms.Item;
import plus.datacenter.core.entities.forms.ItemType;

import java.util.Date;

@Getter
@Setter
public class DateItem extends Item<Date> {

    private Rangeable<Date> range;

    @Override
    public Boolean validate(Date value) {
        return super.validate(value) && (range == null || range.validate(value));
    }

    @Schema(defaultValue = "DATE")
    @Override
    public ItemType getType() {
        return ItemType.DATE;
    }

    @Override
    public void setType(ItemType type) {
        super.setType(ItemType.DATE);
    }
}
