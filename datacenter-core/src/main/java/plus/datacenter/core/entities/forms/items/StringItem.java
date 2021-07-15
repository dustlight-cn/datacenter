package plus.datacenter.core.entities.forms.items;

import lombok.Getter;
import lombok.Setter;
import plus.datacenter.core.entities.forms.Item;

@Getter
@Setter
public class StringItem extends Item {

    private Boolean multiline;
    private Boolean html;
    private String regex;

}
