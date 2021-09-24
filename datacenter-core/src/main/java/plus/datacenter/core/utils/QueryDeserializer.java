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
import plus.datacenter.core.entities.queries.Query;
import plus.datacenter.core.entities.queries.QueryOperation;

import java.io.IOException;
import java.util.HashMap;

@JacksonStdImpl
public class QueryDeserializer extends StdScalarDeserializer<Query> {

    private Gson gson = Converters.registerInstant(new GsonBuilder()).create();

    protected QueryDeserializer() {
        super(Query.class);
    }

    protected QueryDeserializer(Class<?> vc) {
        super(vc);
    }

    protected QueryDeserializer(JavaType valueType) {
        super(valueType);
    }

    protected QueryDeserializer(StdScalarDeserializer<?> src) {
        super(src);
    }

    @Override
    public Query deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {

        HashMap tmp = jsonParser.readValueAs(HashMap.class);
        if (tmp == null)
            return null;
        QueryOperation operation = QueryUtils.getQueryOperation(tmp.get("opt") == null ? "EQUAL" : tmp.get("opt").toString());
        String json = gson.toJson(tmp);
        Query query = gson.fromJson(json, QueryUtils.getQueryClass(operation));
        return query;
    }
}
