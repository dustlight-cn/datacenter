package plus.datacenter.elasticsearch.converters;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;

import java.time.Instant;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LongToInstantConverter implements GenericConverter {

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        Set<ConvertiblePair> convertiblePairs = Collections.newSetFromMap(new ConcurrentHashMap<>());
        convertiblePairs.add(new ConvertiblePair(Long.class, Instant.class));
        return convertiblePairs;
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        return Instant.ofEpochMilli((Long) source);
    }
}
