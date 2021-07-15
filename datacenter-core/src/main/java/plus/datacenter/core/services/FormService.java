package plus.datacenter.core.services;

import plus.datacenter.core.entities.forms.Form;
import reactor.core.publisher.Mono;

public interface FormService {

    <T extends Form> Mono<T> createForm(T origin);

    <T extends Form> Mono<T> updateForm(T target);

    Mono<Void> deleteForm(String id);
}
