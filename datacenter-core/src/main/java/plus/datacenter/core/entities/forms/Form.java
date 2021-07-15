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
    private Date updatedAt;
    private String clientId;
    private String owner;

    private Collection<ItemGroup> groups;
}
