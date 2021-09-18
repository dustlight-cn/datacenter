package plus.datacenter.schema.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import lombok.Getter;
import lombok.Setter;
import plus.datacenter.core.entities.forms.Form;
import plus.datacenter.core.entities.forms.Record;
import plus.datacenter.core.services.DefaultRecordValidator;
import plus.datacenter.core.services.FormService;
import plus.datacenter.core.utils.FormUtils;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;

public class JsonSchemaRecordValidator extends DefaultRecordValidator {

    @Getter
    @Setter
    private JsonSchemaFactory factory;

    private static final ObjectMapper mapper = new ObjectMapper();

    public JsonSchemaRecordValidator(int order, FormService formService, JsonSchemaFactory factory) {
        super(order, formService);
        this.factory = factory;
    }

    public JsonSchemaRecordValidator(FormService formService, JsonSchemaFactory factory) {
        super(formService);
        this.factory = factory;
    }

    @Override
    protected Mono<Context> doValidate(Context context) {
        Form form = context.getForm();
        Record record = context.getRecord();

        JsonSchema schema = factory.getSchema(FormUtils.transformMapToJsonNode(form.getSchema()));

        Map<String, Object> data;
        JsonNode node;
        if (record == null || (data = record.getData()) == null)
            node = NullNode.getInstance();
        else
            node = mapper.convertValue(data, JsonNode.class);

        Set<ValidationMessage> results = schema.validate(node);
        if (results == null || results.size() == 0)
            return Mono.just(context);
        else {
            return Mono.error(ValidationExceptionUtil.getException(results));
        }
    }
}
