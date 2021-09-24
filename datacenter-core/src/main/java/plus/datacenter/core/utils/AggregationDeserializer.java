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
import plus.datacenter.core.entities.queries.Aggregation;
import plus.datacenter.core.entities.queries.AggregationOperation;

import java.io.IOException;
import java.util.HashMap;

@JacksonStdImpl
public class AggregationDeserializer extends StdScalarDeserializer<Aggregation> {

    private Gson gson = Converters.registerInstant(new GsonBuilder()).create();

    protected AggregationDeserializer() {
        super(Aggregation.class);
    }

    protected AggregationDeserializer(Class<?> vc) {
        super(vc);
    }

    protected AggregationDeserializer(JavaType valueType) {
        super(valueType);
    }

    protected AggregationDeserializer(StdScalarDeserializer<?> src) {
        super(src);
    }

    @Override
    public Aggregation deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {

        HashMap tmp = jsonParser.readValueAs(HashMap.class);
        if (tmp == null)
            return null;
        AggregationOperation operation = AggregationUtils.getAggregationOperation(tmp.get("opt") == null ? "TERM" : tmp.get("opt").toString());
        String json = gson.toJson(tmp);
        Aggregation aggregation = gson.fromJson(json, AggregationUtils.getAggregationClass(operation));
        return aggregation;
    }
}
