package plus.datacenter.core.entities.forms.items;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import plus.datacenter.core.entities.Rangeable;
import plus.datacenter.core.entities.forms.Item;
import plus.datacenter.core.entities.forms.ItemType;

import java.time.Instant;

@Getter
@Setter
public class DateItem extends Item<Instant> {

    private Rangeable<Instant> dateRange;

    @Override
    public Boolean validate(Instant value) {
        if (value == null)
            return super.validate(null);
        return super.validate(value) && (dateRange == null || dateRange.validate(value));
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
