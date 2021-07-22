package plus.datacenter.core.services;

import plus.auth.entities.QueryResult;
import plus.datacenter.core.entities.forms.Form;
import reactor.core.publisher.Mono;

public interface FormSearcher {

    Mono<QueryResult<Form>> search(String clientId, String query, int page, int size);

    Mono<QueryResult<Form>> search(String clientId, String query, String name, int page, int size);

}
