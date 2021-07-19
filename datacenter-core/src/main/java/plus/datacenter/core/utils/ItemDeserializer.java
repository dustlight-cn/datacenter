package plus.datacenter.core.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import plus.datacenter.core.entities.forms.Item;
import plus.datacenter.core.entities.forms.ItemType;

import java.io.IOException;
import java.util.HashMap;

@JacksonStdImpl
public class ItemDeserializer extends StdScalarDeserializer<Item> {

    private Gson gson = Converters.registerInstant(new GsonBuilder()).create();

    protected ItemDeserializer() {
        super(Item.class);
    }

    protected ItemDeserializer(Class<?> vc) {
        super(vc);
    }

    protected ItemDeserializer(JavaType valueType) {
        super(valueType);
    }

    protected ItemDeserializer(StdScalarDeserializer<?> src) {
        super(src);
    }

    @Override
    public Item deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {

        HashMap tmp = jsonParser.readValueAs(HashMap.class);
        if (tmp == null)
            return null;
        ItemType type = FormUtils.getItemType(tmp.get("type") == null ? "STRING" : tmp.get("type").toString());
        String json = gson.toJson(tmp);
        Item item = gson.fromJson(json, FormUtils.getItemClass(type));
        return item;
    }
}
