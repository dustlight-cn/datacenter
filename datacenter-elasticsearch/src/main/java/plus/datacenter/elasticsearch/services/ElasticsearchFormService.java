package plus.datacenter.elasticsearch.services;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import plus.auth.entities.QueryResult;
import plus.datacenter.core.entities.forms.Form;
import plus.datacenter.core.services.FormService;
import reactor.core.publisher.Mono;

@Getter
@Setter
@AllArgsConstructor
public class ElasticsearchFormService implements FormService {

    @Override
    public Mono<Form> createForm(Form origin) {
        return null;
    }

    @Override
    public Mono<Form> getForm(String name, String clientId) {
        return null;
    }

    @Override
    public Mono<Form> getFormById(String id,String clientId) {
        return null;
    }

    @Override
    public Mono<Form> updateForm(Form target) {
        return null;
    }

    @Override
    public Mono<Void> deleteForm(String name, String clientId) {
        return null;
    }

    @Override
    public Mono<QueryResult<Form>> listForm(String clientId) {
        return null;
    }

    @Override
    public Mono<QueryResult<Form>> listForm(String clientId, String name) {
        return null;
    }
}
