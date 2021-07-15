package plus.datacenter.core.entities.forms;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Collection;

@Getter
@Setter
public class ItemGroup implements Serializable {

    private String name;
    private String description;

    private Collection<Item> items;

}
