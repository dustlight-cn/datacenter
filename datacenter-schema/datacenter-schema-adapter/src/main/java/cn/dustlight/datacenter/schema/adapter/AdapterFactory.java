package cn.dustlight.datacenter.schema.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.*;
import com.networknt.schema.uri.ClasspathURLFactory;
import com.networknt.schema.uri.URLFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import cn.dustlight.datacenter.schema.Schemas;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;


@Getter
@Setter
@AllArgsConstructor
public class AdapterFactory {

    // Draft 6 uses "$id"
    private static final String ID = "$id";
    private static final List<Format> BUILTIN_FORMATS = new ArrayList<>(JsonMetaSchema.COMMON_BUILTIN_FORMATS);

    private Schemas schemas;

    private String metaSchema;

    private Set<String> nonValidationKeywords;

    private JsonMetaSchema jsonMetaSchema() {
        Map<String, Schemas.Schema> map;
        Schemas.Schema schema;
        JsonNode node;
        if (schemas == null || metaSchema == null ||
                (map = schemas.getSchemaMap()) == null ||
                (schema = map.get(metaSchema)) == null ||
                (node = schema.getJsonNode()) == null)
            throw new RuntimeException("Fail to get meta node");
        if (!node.has(ID))
            throw new RuntimeException(String.format("Meta node missing field '%s'", ID));
        String id = node.get(ID).asText();
        String uri = removeSharp(id);
        Collection<Keyword> keywords = new HashSet<>();
        if (nonValidationKeywords != null && nonValidationKeywords.size() > 0)
            for (String kw : nonValidationKeywords)
                keywords.add(new NonValidationKeyword(kw));
        return new JsonMetaSchema.Builder(uri)
                .idKeyword(ID)
                .addFormats(BUILTIN_FORMATS)
                .addKeywords(ValidatorTypeCode.getNonFormatKeywords(SpecVersion.VersionFlag.V6))
                // keywords that may validly exist, but have no validation aspect to them
                .addKeywords(Arrays.asList(
                        new NonValidationKeyword("$schema"),
                        new NonValidationKeyword("$id"),
                        new NonValidationKeyword("title"),
                        new NonValidationKeyword("description"),
                        new NonValidationKeyword("default"),
                        new NonValidationKeyword("definitions")
                ))
                .addKeywords(keywords)
                .build();
    }

    private static String removeSharp(String input) {
        int sharpIndex = input.lastIndexOf('#');
        return sharpIndex == -1 ? input : input.substring(0, sharpIndex);
    }

    private String[] uriSchemas() {
        Set<String> set = new HashSet<>();

        set.addAll(ClasspathURLFactory.SUPPORTED_SCHEMES);
        set.addAll(URLFactory.SUPPORTED_SCHEMES);
        String[] result = new String[set.size()];
        set.toArray(result);
        return result;
    }

    private Map<String, byte[]> uriMap() {
        Map<String, byte[]> result = new HashMap<>();
        if (schemas == null || schemas.getSchemaMap() == null)
            throw new NullPointerException("Schema can not be null");
        Collection<Schemas.Schema> schemaz = schemas.getSchemaMap().values();
        for (Schemas.Schema schema : schemaz) {
            JsonNode node = schema.getJsonNode();
            if (node == null)
                throw new NullPointerException("Schema can not be null");
            if (!node.has(ID))
                throw new RuntimeException(String.format("Meta node missing field '%s'", ID));
            result.put(removeSharp(node.get(ID).asText()),
                    node.toString().getBytes(StandardCharsets.UTF_8));
        }
        return result;
    }

    private JsonSchemaFactory jsonSchemaFactory() {
        JsonMetaSchema metaSchema = jsonMetaSchema();
        Map<String, byte[]> uriMap = uriMap();
        return new JsonSchemaFactory.Builder()
                .defaultMetaSchemaURI(metaSchema.getUri())
                .uriFetcher(uri -> {
                            String key = uri.toASCIIString();
                            if (!uriMap.containsKey(key))
                                throw new RuntimeException(String.format("URI '%s' is not allow", key));
                            return new ByteArrayInputStream(uriMap.get(key));
                        },
                        uriSchemas())
                .addMetaSchema(metaSchema)
                .addMetaSchema(JsonMetaSchema.getV6())
                .build();
    }

    public static JsonSchemaFactory get(Schemas schemas, String metaKey, Set<String> nonValidationKeywords) {
        return new AdapterFactory(schemas, metaKey, nonValidationKeywords).jsonSchemaFactory();
    }

    public static JsonSchemaFactory get(Schemas schemas, String metaKey, String... nonValidationKeywords) {
        return new AdapterFactory(schemas, metaKey, Set.of(nonValidationKeywords)).jsonSchemaFactory();
    }
}
