package plus.datacenter.core.entities.forms;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
public class FormRecord {

    private String id;
    private String clientId;
    private String owner;

    private String formId;
    private String formName;
    private Integer formVersion;

    private Instant createdAt;
    private Instant updatedAt;

    private Map<String, Object> data;
}