package plus.datacenter.core.entities.forms.items;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import plus.datacenter.core.entities.forms.Item;
import plus.datacenter.core.entities.forms.ItemType;

import java.util.Collection;

@Getter
@Setter
public class ElasticItem extends Item<ElasticItem.ElasticValue> {

    private Collection<Item> options;

    @Schema(defaultValue = "ELASTIC")
    @Override
    public ItemType getType() {
        return ItemType.ELASTIC;
    }

    @Override
    public void setType(ItemType type) {
        super.setType(ItemType.ELASTIC);
    }

    @Override
    public Boolean validate(ElasticValue value) {
        if (!super.validate(value))
            return false;
        if (options == null || options.size() == 0 || value.name == null)
            return false;
        for (Item item : options)
            if (value.name.equals(item.getName()))
                return item.validate(value.value);
        return false;
    }

    @Getter
    @Setter
    public static class ElasticValue {
        private String name;
        private Object value;
    }
}
