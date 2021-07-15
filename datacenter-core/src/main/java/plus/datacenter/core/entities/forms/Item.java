package plus.datacenter.core.entities.forms;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class Item implements Serializable {


    private String name;
    private String description;
    private ItemType type;
    private Boolean array;

}
