package plus.datacenter.core.services;

import plus.datacenter.core.entities.forms.Form;
import reactor.core.publisher.Mono;

public interface FormService {

    Mono<Form> createForm(Form origin);

    Mono<Form> updateForm(Form target);

    Mono<Void> deleteForm(String id);
}
