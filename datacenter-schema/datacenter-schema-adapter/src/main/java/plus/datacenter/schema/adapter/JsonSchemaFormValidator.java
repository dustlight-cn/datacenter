package plus.datacenter.schema.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import plus.datacenter.core.entities.DatacenterPrincipal;
import plus.datacenter.core.entities.forms.Form;
import plus.datacenter.core.services.AbstractFormValidator;
import plus.datacenter.core.utils.FormUtils;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class JsonSchemaFormValidator extends AbstractFormValidator {

    private JsonSchema formSchema;

    @Override
    public Mono<Collection<Form>> validate(Collection<Form> forms, DatacenterPrincipal principal, String clientId) {
        for (Form form : forms) {
            JsonNode formNode = FormUtils.transformMapToJsonNode(form.getSchema());
            Set<ValidationMessage> msgs = formSchema.validate(formNode);
            if (form.getSchema() != null) {
                form.getSchema().remove("$id");
                form.getSchema().remove("$schema");
            }
            if (msgs != null && msgs.size() != 0)
                return Mono.error(ValidationExceptionUtil.getException(msgs));
        }
        return Mono.just(forms);
    }

}
