package cn.dustlight.datacenter.core.entities.forms;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
public class Record implements Cloneable {

    private static final Record template = new Record();

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

    public Record castToRecord() throws CloneNotSupportedException {
        if (getClass() == Record.class)
            return this;
        Record newRecord = (Record) template.clone();

        newRecord.id = this.id;
        newRecord.clientId = this.clientId;
        newRecord.owner = this.owner;

        newRecord.formId = this.formId;
        newRecord.formName = this.formName;
        newRecord.formVersion = this.formVersion;


        newRecord.createdAt = this.createdAt;
        newRecord.updatedAt = this.updatedAt;

        newRecord.data = this.data;

        return newRecord;
    }

    @Override
    public String toString() {
        return "Record{" +
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
