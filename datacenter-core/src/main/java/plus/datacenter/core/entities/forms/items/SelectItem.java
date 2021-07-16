package plus.datacenter.core.entities.forms.items;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import plus.datacenter.core.entities.forms.Item;
import plus.datacenter.core.entities.forms.ItemType;

import java.util.HashSet;
import java.util.Map;

@Getter
@Setter
public class SelectItem extends Item<SelectItem.Selected> {

    private Integer max;
    private Map<String, String> options;

    @Schema(defaultValue = "SELECT")
    @Override
    public ItemType getType() {
        return ItemType.SELECT;
    }

    @Override
    public void setType(ItemType type) {
        super.setType(ItemType.SELECT);
    }

    public static class Selected extends HashSet<String> {

    }
}
