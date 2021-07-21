package plus.datacenter.core.entities.forms.items;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import plus.datacenter.core.entities.forms.Item;
import plus.datacenter.core.entities.forms.ItemType;

@Getter
@Setter
public class UserItem extends Item<String> {

    @Schema(defaultValue = "USER")
    @Override
    public ItemType getType() {
        return ItemType.USER;
    }

    @Override
    public void setType(ItemType type) {
        super.setType(ItemType.USER);
    }
}
