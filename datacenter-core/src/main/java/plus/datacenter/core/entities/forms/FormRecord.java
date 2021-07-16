package plus.datacenter.core.entities.forms;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Map;

@Getter
@Setter
public class FormRecord {

    private String id;
    private String formId;
    private String clientId;
    private String owner;

    private Date createdAt;
    private Date updatedAt;

    private Map<String, Object> data;

}
