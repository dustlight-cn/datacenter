package plus.datacenter.elasticsearch.services;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.util.StringUtils;
import plus.auth.entities.QueryResult;
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
        Query nsq = new NativeSearchQuery(bq).setPageable(Pageable.ofSize(size).withPage(page));
        IndexCoordinates indexCoordinates = IndexCoordinates.of(indexPrefix);
        return operations.searchForPage(nsq
                        , Form.class, indexCoordinates)
                .map(searchHits ->
                        new QueryResult((int) searchHits.getTotalElements(),
                                searchHits.getContent().stream().map(formSearchHit ->
                                        formSearchHit.getContent()).collect(Collectors.toList())));
    }

    public Mono<QueryResult<Form>> search(String clientId, String query, String name, int page, int size) {
        BoolQueryBuilder bq = new BoolQueryBuilder().must(
                        StringUtils.hasText(query) ?
                                new MultiMatchQueryBuilder(query, "schema.title", "schema.description") :
                                new MatchAllQueryBuilder())
                .filter(new MatchQueryBuilder("clientId", clientId))
                .filter(new MatchQueryBuilder("name", name));
        Query nsq = new NativeSearchQuery(bq).setPageable(Pageable.ofSize(size).withPage(page));
        IndexCoordinates indexCoordinates = IndexCoordinates.of(indexPrefix);
        return operations.searchForPage(nsq
                        , Form.class, indexCoordinates)
                .map(searchHits ->
                        new QueryResult((int) searchHits.getTotalElements(),
                                searchHits.getContent().stream().map(formSearchHit ->
                                        formSearchHit.getContent()).collect(Collectors.toList())));
    }
}
