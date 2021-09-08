package plus.datacenter.core.entities.forms;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
public class Record implements Cloneable {

    private String id;
    private String clientId;
    private String owner;

    private String formId;
    private String formName;
    private Integer formVersion;

    private Instant createdAt;
    private Instant updatedAt;

    private Map<String, Object> data;

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return "FormRecord{" +
                "id='" + id + '\'' +
                ", clientId='" + clientId + '\'' +
                ", owner='" + owner + '\'' +
                ", formId='" + formId + '\'' +
                ", formName='" + formName + '\'' +
                ", formVersion=" + formVersion +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", data=" + data +
                '}';
    }
}
