package cn.dustlight.datacenter.amqp.entities;

import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import cn.dustlight.datacenter.core.entities.forms.Record;
import cn.dustlight.datacenter.core.services.RecordEventHandler;

import java.io.Serializable;
import java.util.Collection;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RecodeEvent implements Serializable {

    private final static Gson gson = Converters.registerInstant(new GsonBuilder()).create();

    private RecordEventHandler.EventType type;
    private Collection<Record> records;

    public static RecodeEvent create(RecordEventHandler.EventType type,
                                     Collection<Record> records) {
        return new RecodeEvent(type, records);
    }

    public static RecodeEvent fromJson(String json) {
        return gson.fromJson(json, RecodeEvent.class);
    }

    public static RecodeEvent fromJson(byte[] json) {
        return gson.fromJson(new String(json), RecodeEvent.class);
    }

    public String toJson() {
        return gson.toJson(this);
    }
}
