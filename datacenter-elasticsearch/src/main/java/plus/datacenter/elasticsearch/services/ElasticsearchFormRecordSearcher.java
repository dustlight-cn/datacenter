package plus.datacenter.elasticsearch.services;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.elasticsearch.index.query.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import plus.auth.entities.QueryResult;
import plus.datacenter.core.entities.forms.FormRecord;
import plus.datacenter.core.entities.queries.Query;
import plus.datacenter.core.entities.queries.queries.BetweenQuery;
import plus.datacenter.core.services.FormRecordSearcher;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
public class ElasticsearchFormRecordSearcher implements FormRecordSearcher {

    private ReactiveElasticsearchOperations operations;
    private String indexPrefix;

    @Override
    public Mono<QueryResult<FormRecord>> findRecord(String clientId,
                                                    String formName,
                                                    Collection<Query> queries,
                                                    Collection<String> orders,
                                                    int page,
                                                    int size) {
        IndexCoordinates index = IndexCoordinates.of(indexPrefix + "." + clientId + "." + formName + ".*");
        BoolQueryBuilder bq = new BoolQueryBuilder();

        if (queries != null) {
            for (Query query : queries) {
                QueryBuilder queryBuilder = transform(query);
                bq.must(queryBuilder);
            }
        }

        NativeSearchQuery nqb = new NativeSearchQuery(bq).setPageable(Pageable.ofSize(size).withPage(page));
        return operations.searchForPage(nqb,
                FormRecord.class,
                index)
                .map(searchHits -> new QueryResult((int) searchHits.getTotalElements(),
                        searchHits.getContent().stream().map(formSearchHit ->
                                formSearchHit.getContent()).collect(Collectors.toList())));
    }

    protected QueryBuilder transform(Query query) {
        switch (query.getOpt()) {
            case MATCH:
                return new MatchQueryBuilder(query.getName(), query.getValue());
            case BETWEEN:
                RangeQueryBuilder rq = new RangeQueryBuilder(query.getName());
                BetweenQuery bq = (BetweenQuery) query;
                if (bq.getValue() != null) {
                    if (bq.getValue().isOpenInterval()) {
                        if (bq.getValue().getMin() != null)
                            rq.gt(bq.getValue().getMin());
                        if (bq.getValue().getMax() != null)
                            rq.lt(bq.getValue().getMax());
                    } else {
                        if (bq.getValue().getMin() != null)
                            rq.gte(bq.getValue().getMin());
                        if (bq.getValue().getMax() != null)
                            rq.lte(bq.getValue().getMax());
                    }
                }

                return rq;
            case IN:
                return new TermsQueryBuilder(query.getName(), (Collection) query.getValue());
            case EQUAL:
            default:
                return new TermQueryBuilder(query.getName(), query.getValue());
        }
    }
}
