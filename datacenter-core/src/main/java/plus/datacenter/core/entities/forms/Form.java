package plus.datacenter.core.entities.forms;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
public class Form implements Serializable {

    private String id;
    private Integer version;
    private Instant createdAt;

    private String clientId;
    private String owner;

    private String name;

    private String primaryKey;

    private Map<String, Object> schema;

    private String additional;

    @Hidden
    @JsonIgnore
    private Set<String> references;

    @Hidden
    @JsonIgnore
    private Map<String, String> referenceMap;
}
