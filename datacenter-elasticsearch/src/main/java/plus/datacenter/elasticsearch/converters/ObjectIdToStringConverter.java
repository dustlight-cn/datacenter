package plus.datacenter.elasticsearch.converters;

import org.bson.types.ObjectId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class ObjectIdToStringConverter implements Converter<ObjectId,String> {

    @Override
    public String convert(ObjectId source) {
        return source.toHexString();
    }
}
