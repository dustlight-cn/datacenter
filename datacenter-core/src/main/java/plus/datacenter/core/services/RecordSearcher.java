package plus.datacenter.core.services;

import plus.auth.entities.QueryResult;
import plus.datacenter.core.entities.forms.Record;
import plus.datacenter.core.entities.queries.Aggregation;
import plus.datacenter.core.entities.queries.Query;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;

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
