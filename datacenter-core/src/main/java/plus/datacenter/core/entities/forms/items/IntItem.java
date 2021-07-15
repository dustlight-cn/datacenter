package plus.datacenter.core.entities.forms.items;

import lombok.Getter;
import lombok.Setter;
import plus.datacenter.core.entities.Rangeable;
import plus.datacenter.core.entities.forms.Item;

@Getter
@Setter
public class IntItem extends Item {

    private Rangeable<Integer> range;

}
