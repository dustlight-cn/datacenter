package plus.datacenter.core.entities.forms;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class Form implements Serializable {

    private String id;
    private Integer version;
    private Instant createdAt;

    private String clientId;
    private String owner;

    private String name;
    private String label;
    private String description;

    private String primaryKey;

    private Collection<ItemGroup> groups;

    @JsonIgnore
    public Map<String, Item> getItems() {
        HashMap<String, Item> map = new HashMap<>();
        if (groups != null)
            for (ItemGroup group : groups) {
                Collection<Item> items = group.getItems();
                if (items == null)
                    continue;
                for (Item item : items)
                    map.put(item.getName(), item);
            }
        return map;
    }
}
