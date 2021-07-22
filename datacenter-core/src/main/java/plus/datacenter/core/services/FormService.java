package plus.datacenter.core.services;

import plus.auth.entities.QueryResult;
import plus.datacenter.core.entities.forms.Form;
import reactor.core.publisher.Mono;

public interface FormService {

    Mono<Form> createForm(Form origin);

    Mono<Form> getForm(String name, String clientId);

    Mono<Form> getFormById(String id);

    Mono<Form> updateForm(Form target);

    Mono<Void> deleteForm(String name, String clientId);

    Mono<QueryResult<Form>> listForm(String clientId);

    Mono<QueryResult<Form>> listForm(String clientId, String name);
}
