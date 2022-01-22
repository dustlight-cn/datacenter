package cn.dustlight.datacenter.mongo.converters;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.bson.types.ObjectId;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;

/**
 * 在 Json 序列化时，将 ObjectID 类型的值转换成十六进制字符串。
 */
@JsonComponent
public class ObjectIdToStringSerializer extends JsonSerializer<ObjectId> {

    @Override
    public void serialize(ObjectId objectId, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(objectId == null ? null : objectId.toHexString());
    }
}
