package plus.datacenter.application.configurations;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import plus.datacenter.application.services.FormSchemaFiller;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(FormResourceConfiguration.FormSchemaProperties.class)
public class FormResourceConfiguration {

    @Bean
    public FormSchemaFiller formSchemaFiller(@Autowired FormSchemaProperties properties) {
        return new FormSchemaFiller(KeyValue.toMap(properties.getSchemaAdditional()));
    }

    @Getter
    @Setter
    @ConfigurationProperties(prefix = "plus.datacenter.form")
    public static class FormSchemaProperties {

        public KeyValue<String,Object>[] schemaAdditional;

    }

    @Getter
    @Setter
    public static class KeyValue<K, V> {
        private K key;
        private V value;

        public static <K, V> Map<K, V> toMap(KeyValue<K, V>... keyValues) {
            HashMap<K, V> map = new HashMap<>();
            if (keyValues != null)
                for (KeyValue<K, V> kv : keyValues)
                    map.put(kv.key, kv.value);
            return map;
        }
    }
}
