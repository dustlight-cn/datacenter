package plus.datacenter.elasticsearch.services;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.search.aggregations.metrics.CardinalityAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.ParsedCardinality;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.util.StringUtils;
import cn.dustlight.auth.entities.QueryResult;
import plus.datacenter.core.entities.forms.Form;
import plus.datacenter.core.services.FormSearcher;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
public class ElasticsearchFormSearcher implements FormSearcher {

    private ReactiveElasticsearchOperations operations;
    private String indexPrefix;

    public Mono<QueryResult<Form>> search(String clientId, String query, int page, int size) {
        BoolQueryBuilder bq = new BoolQueryBuilder().must(
                        StringUtils.hasText(query) ?
                                new MultiMatchQueryBuilder(query, "name", "schema.title", "schema.description") :
                                new MatchAllQueryBuilder())
                .filter(new MatchQueryBuilder("clientId", clientId));
        NativeSearchQuery nsq = new NativeSearchQueryBuilder()
                .withQuery(bq)
                .withPageable(Pageable.ofSize(size).withPage(page))
                .withSort(new FieldSortBuilder("version").order(SortOrder.DESC))
                .withSort(new FieldSortBuilder("createdAt").order(SortOrder.DESC))
                .withCollapseField("name.keyword")
                .addAggregation(new CardinalityAggregationBuilder("count").field("name.keyword"))
                .build();
        IndexCoordinates indexCoordinates = IndexCoordinates.of(indexPrefix);
        return operations.searchForPage(nsq
                        , Form.class, indexCoordinates)
                .map(searchHits ->
                {
                    long count = searchHits.getSearchHits().getAggregations().get("count") instanceof ParsedCardinality ?
                            ((ParsedCardinality) searchHits.getSearchHits().getAggregations().get("count")).getValue() :
                            searchHits.getTotalElements();
                    return new QueryResult((int) count,
                            searchHits.getContent().stream().map(formSearchHit ->
                                    formSearchHit.getContent()).collect(Collectors.toList()));
                });
    }

    public Mono<QueryResult<Form>> search(String clientId, String query, String name, int page, int size) {
        BoolQueryBuilder bq = new BoolQueryBuilder().must(
                        StringUtils.hasText(query) ?
                                new MultiMatchQueryBuilder(query, "schema.title", "schema.description") :
                                new MatchAllQueryBuilder())
                .filter(new MatchQueryBuilder("clientId", clientId))
                .filter(new MatchQueryBuilder("name", name));
        NativeSearchQuery nsq = new NativeSearchQueryBuilder()
                .withQuery(bq)
                .withPageable(Pageable.ofSize(size).withPage(page))
                .withSort(new FieldSortBuilder("createdAt").order(SortOrder.DESC))
                .build();
        IndexCoordinates indexCoordinates = IndexCoordinates.of(indexPrefix);
        return operations.searchForPage(nsq
                        , Form.class, indexCoordinates)
                .map(searchHits ->
                        new QueryResult((int) searchHits.getTotalElements(),
                                searchHits.getContent().stream().map(formSearchHit ->
                                        formSearchHit.getContent()).collect(Collectors.toList())));
    }
}
