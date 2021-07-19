package plus.datacenter.elasticsearch.services;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import plus.auth.entities.QueryResult;
import plus.datacenter.core.entities.forms.Form;
import plus.datacenter.core.services.FormSearcher;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
public class ElasticsearchFormSearcher implements FormSearcher {

    ReactiveElasticsearchOperations operations;


    public Mono<QueryResult<Form>> search(String clientId, String query, int page, int size) {

        IndexCoordinates indexCoordinates = IndexCoordinates.of("datacenter.form");
        return operations.searchForPage(new CriteriaQuery(Criteria.where("clientId").is(clientId)
                        .and(Criteria.where("name").matches(query)
                                .or("label").matches(query)
                                .or("description").matches(query)))
                        .setPageable(Pageable.ofSize(size).withPage(page))
                , Form.class, indexCoordinates)
                .map(searchHits ->
                        new QueryResult((int) searchHits.getTotalElements(),
                                searchHits.getContent().stream().map(formSearchHit ->
                                        formSearchHit.getContent()).collect(Collectors.toList())));
    }
}
