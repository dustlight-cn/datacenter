package plus.datacenter.core.entities.forms;

import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.Date;

@Getter
@Setter
public class Form {

    private String id;
    private Integer version;
    private Date createdAt;
    private String clientId;
    private String owner;

    private String name;
    private String label;
    private String description;

    private Collection<ItemGroup> groups;
}
