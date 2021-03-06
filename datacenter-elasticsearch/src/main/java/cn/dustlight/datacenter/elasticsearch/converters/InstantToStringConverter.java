package cn.dustlight.datacenter.elasticsearch.converters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import java.time.Instant;

@WritingConverter
public class InstantToStringConverter implements Converter<Instant,String> {

    @Override
    public String convert(Instant source) {
        return source.toString();
    }
}
