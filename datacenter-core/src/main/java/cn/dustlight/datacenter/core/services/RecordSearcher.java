package cn.dustlight.datacenter.core.services;

import cn.dustlight.auth.entities.QueryResult;
import cn.dustlight.datacenter.core.entities.queries.Aggregation;
import cn.dustlight.datacenter.core.entities.queries.Query;
import cn.dustlight.datacenter.core.entities.forms.Record;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;

/**
 * 记录搜索器，负责复杂的记录搜索。
 */
public interface RecordSearcher {

    Mono<QueryResult<Record>> findRecord(String clientId,
                                         String formName,
                                         Collection<Query> queries,
                                         List<String> orders,
                                         int page,
                                         int size);

    Mono<?> aggregate(String clientId,
                      String formName,
                      Collection<Query> queries,
                      Aggregation aggregation);
}
