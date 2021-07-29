package plus.datacenter.core.entities.forms;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Map;

import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Setter
public class FormRecord {

    private String id;
    private String clientId;
    private String owner;

    private String formId;
    private String formName;
    private Integer formVersion;

    @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
    private Instant createdAt;

    @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
    private Instant updatedAt;

    private Map<String, Object> data;
}
