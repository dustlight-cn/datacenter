package plus.datacenter.core.entities.forms.items;

import lombok.Getter;
import lombok.Setter;
import plus.datacenter.core.entities.forms.Item;

import java.util.Collection;
import java.util.Map;

@Getter
@Setter
public class SelectItem extends Item {

    private Integer max;
    private Map<String, String> options;

}
