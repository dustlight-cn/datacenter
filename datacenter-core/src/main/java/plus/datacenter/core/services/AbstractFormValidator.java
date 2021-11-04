package plus.datacenter.core.services;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import plus.datacenter.core.ErrorEnum;
import plus.datacenter.core.entities.DatacenterPrincipal;
import plus.datacenter.core.entities.forms.Form;
import reactor.core.publisher.Mono;

import java.util.Collection;

@AllArgsConstructor
@NoArgsConstructor
public abstract class AbstractFormValidator implements FormValidator {

    private int order = 0;

    @Override
    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public Mono<Collection<Form>> validate(Collection<Form> forms, DatacenterPrincipal principal, String clientId) {
        try {
            return doValidate(forms, principal, clientId);
        } catch (Throwable e) {
            return Mono.error(ErrorEnum.SCHEMA_INVALID.details(e).getException());
        }
    }

    protected abstract Mono<Collection<Form>> doValidate(Collection<Form> forms, DatacenterPrincipal principal, String clientId) throws Throwable;
}
