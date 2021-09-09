package plus.datacenter.core.services;

import plus.auth.entities.QueryResult;
import plus.datacenter.core.entities.forms.Form;
import reactor.core.publisher.Mono;

/**
 * 表单搜索器，负责复杂的表单搜索。
 */
public interface FormSearcher {

    Mono<QueryResult<Form>> search(String clientId, String query, int page, int size);

    Mono<QueryResult<Form>> search(String clientId, String query, String name, int page, int size);

}
