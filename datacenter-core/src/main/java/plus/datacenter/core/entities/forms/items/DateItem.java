package plus.datacenter.core.entities.forms.items;

import lombok.Getter;
import lombok.Setter;
import plus.datacenter.core.entities.Rangeable;
import plus.datacenter.core.entities.forms.Item;

import java.util.Date;

@Getter
@Setter
public class DateItem extends Item {

    private Rangeable<Date> range;
    
}
