package plus.datacenter.jsonschema;

import com.networknt.schema.*;

import java.util.*;

public class V6DC {

    private static String URI = "https://live-hls.dustlight.cn/live/draft-06-dc.json";

    // Draft 6 uses "$id"
    private static final String ID = "$id";

    public static final List<Format> BUILTIN_FORMATS = new ArrayList<Format>(JsonMetaSchema.COMMON_BUILTIN_FORMATS);

    static {
        // add version specific formats here.
        //BUILTIN_FORMATS.add(pattern("phone", "^\\+(?:[0-9] ?){6,14}[0-9]$"));
    }

    public static JsonSchemaFactory factory() {
        JsonMetaSchema metaSchema = getInstance();

        return new JsonSchemaFactory.Builder()
                .defaultMetaSchemaURI(metaSchema.getUri())
                .addMetaSchema(metaSchema)
                .build();
    }

    private static JsonMetaSchema getInstance() {
        System.out.println(URI);
        return new JsonMetaSchema.Builder(URI)
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
                .build();
    }
}
