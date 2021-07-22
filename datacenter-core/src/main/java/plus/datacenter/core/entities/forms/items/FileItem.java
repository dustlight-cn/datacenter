package plus.datacenter.core.entities.forms.items;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import plus.datacenter.core.entities.forms.Item;
import plus.datacenter.core.entities.forms.ItemType;

@Getter
@Setter
public class FileItem extends Item<String> {

    private String mime;

    @Schema(defaultValue = "FILE")
    @Override
    public ItemType getType() {
        return ItemType.FILE;
    }

    @Override
    public void setType(ItemType type) {
        super.setType(ItemType.FILE);
    }
}
