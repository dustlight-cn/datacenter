package cn.dustlight.datacenter.core.services;

import cn.dustlight.auth.entities.QueryResult;
import cn.dustlight.datacenter.core.entities.forms.Form;
import reactor.core.publisher.Mono;

/**
 * 表单搜索器，负责复杂的表单搜索。
 */
public interface FormSearcher {

    Mono<QueryResult<Form>> search(String clientId, String query, int page, int size);

    Mono<QueryResult<Form>> search(String clientId, String query, String name, int page, int size);

}
