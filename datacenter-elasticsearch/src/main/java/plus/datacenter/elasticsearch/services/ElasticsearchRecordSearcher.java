package plus.datacenter.elasticsearch.services;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.HistogramAggregationBuilder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import cn.dustlight.auth.entities.QueryResult;
import plus.datacenter.core.entities.forms.Record;
import plus.datacenter.core.entities.queries.Aggregation;
import plus.datacenter.core.entities.queries.Query;
import plus.datacenter.core.entities.queries.aggs.DateHistogramAggregation;
import plus.datacenter.core.entities.queries.aggs.HistogramAggregation;
import plus.datacenter.core.entities.queries.queries.BetweenQuery;
import plus.datacenter.core.services.RecordSearcher;
import plus.datacenter.elasticsearch.utils.OrderUtil;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
public class ElasticsearchRecordSearcher implements RecordSearcher {

    private ReactiveElasticsearchOperations operations;
    private String indexPrefix;

    @Override
    public Mono<QueryResult<Record>> findRecord(String clientId,
                                                String formName,
                                                Collection<Query> queries,
                                                List<String> orders,
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

        if (orders != null && orders.size() > 0) {
            nqb.addSort(Sort.by(OrderUtil.getOrders(orders)));
        }
        return operations.searchForPage(nqb,
                Record.class,
                index)
                .map(searchHits -> new QueryResult((int) searchHits.getTotalElements(),
                        searchHits.getContent().stream().map(formSearchHit ->
                                formSearchHit.getContent()).collect(Collectors.toList())));
    }

    @Override
    public Mono<?> aggregate(String clientId, String formName, Collection<Query> queries, Aggregation aggregation) {
        IndexCoordinates index = IndexCoordinates.of(indexPrefix + "." + clientId + "." + formName + ".*");

        BoolQueryBuilder bq = new BoolQueryBuilder();

        if (queries != null) {
            for (Query query : queries) {
                QueryBuilder queryBuilder = transform(query);
                bq.must(queryBuilder);
            }
        }

        NativeSearchQuery nqb = new NativeSearchQuery(bq);
        if(aggregation != null)
            nqb.addAggregation(transform(aggregation));

        return operations.aggregate(nqb, Record.class, index)
                .collectList();
    }

    public static QueryBuilder transform(Query query) {
        switch (query.getOpt()) {
            case MATCH:
                String[] keys = query.getName().split(",");
                if (keys.length == 1)
                    return new MatchQueryBuilder(query.getName(), query.getValue());
                else
                    return new MultiMatchQueryBuilder(query.getValue(), keys);
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

    public static AbstractAggregationBuilder transform(Aggregation aggregation) {
        if (aggregation == null)
            return null;
        AbstractAggregationBuilder builder = null;
        switch (aggregation.getOpt()) {
            case MAX:
                builder = AggregationBuilders.max(aggregation.getName()).field(aggregation.getField());
                break;
            case MIN:
                builder = AggregationBuilders.min(aggregation.getName()).field(aggregation.getField());
                break;
            case SUM:
                builder = AggregationBuilders.sum(aggregation.getName()).field(aggregation.getField());
                break;
            case COUNT:
                builder = AggregationBuilders.count(aggregation.getName()).field(aggregation.getField());
                break;
            case AVG:
                builder = AggregationBuilders.avg(aggregation.getName()).field(aggregation.getField());
                break;
            case HISTOGRAM:
                builder = transformHistogramAggregationBuilder((HistogramAggregation) aggregation);
                break;
            case DATE_HISTOGRAM:
                builder = transformDateHistogramAggregationBuilder((DateHistogramAggregation) aggregation);
                break;
            case TERM:
            default:
                builder = AggregationBuilders.terms(aggregation.getName()).field(aggregation.getField());
                break;
        }
        if (builder == null)
            return null;
        if (aggregation.getSubAgg() != null) {
            builder.subAggregation(transform(aggregation.getSubAgg()));
        }
        return builder;
    }

    public static HistogramAggregationBuilder transformHistogramAggregationBuilder(HistogramAggregation aggregation) {
        return AggregationBuilders.histogram(aggregation.getName()).field(aggregation.getField()).interval(aggregation.getInterval());
    }

    public static DateHistogramAggregationBuilder transformDateHistogramAggregationBuilder(DateHistogramAggregation aggregation) {
        DateHistogramInterval interval = aggregation.getUnit() != null ?
                new DateHistogramInterval(aggregation.getInterval() + aggregation.getUnit().getValue()) :
                new DateHistogramInterval(aggregation.getInterval() + DateHistogramAggregation.IntervalUnit.DAY.getValue());
        return AggregationBuilders.dateHistogram(aggregation.getName()).field(aggregation.getField()).calendarInterval(interval);
    }
}
