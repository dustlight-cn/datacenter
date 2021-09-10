package plus.datacenter.amqp.entities;

import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import plus.datacenter.core.entities.forms.Record;
import plus.datacenter.core.services.RecordEventHandler;

import java.io.Serializable;
import java.util.Collection;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RecodeEventMessage implements Serializable {

    private final static Gson gson = Converters.registerInstant(new GsonBuilder()).create();

    private RecordEventHandler.EventType type;
    private Collection<Record> records;

    public static RecodeEventMessage create(RecordEventHandler.EventType type,
                                            Collection<Record> records) {
        return new RecodeEventMessage(type, records);
    }

    public static RecodeEventMessage fromJson(String json) {
        return gson.fromJson(json, RecodeEventMessage.class);
    }

    public static RecodeEventMessage fromJson(byte[] json) {
        return gson.fromJson(new String(json), RecodeEventMessage.class);
    }

    public String toJson() {
        return gson.toJson(this);
    }
}
