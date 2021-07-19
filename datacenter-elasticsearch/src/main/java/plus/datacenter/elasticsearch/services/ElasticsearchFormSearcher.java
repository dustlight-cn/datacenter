package plus.datacenter.elasticsearch.services;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import plus.datacenter.core.entities.forms.Form;
import plus.datacenter.core.services.FormSearcher;
import reactor.core.publisher.Mono;

@Getter
@Setter
@AllArgsConstructor
public class ElasticsearchFormSearcher implements FormSearcher {

    ReactiveElasticsearchOperations operations;


    public Mono<?> test(String q) {
        IndexCoordinates indexCoordinates = IndexCoordinates.of("datacenter.form");
        return operations.searchForPage(new CriteriaQuery(Criteria.where("name").matches(q)
                        .or("label").matches(q)
                        .or("description").matches(q))
                , Form.class, indexCoordinates);
    }
}
