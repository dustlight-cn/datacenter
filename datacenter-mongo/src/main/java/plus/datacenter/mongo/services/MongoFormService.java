package plus.datacenter.mongo.services;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import plus.datacenter.core.entities.forms.Form;
import plus.datacenter.core.services.FormService;
import reactor.core.publisher.Mono;

@Getter
@Setter
@AllArgsConstructor
public class MongoFormService implements FormService {

    private ReactiveMongoOperations operations;
    private String collectionName;

    @Override
    public Mono<Form> createForm(Form origin) {
        origin.setId(null);
        return operations.insert(origin, collectionName);
    }

    @Override
    public Mono<Form> updateForm(Form target) {
        return operations.save(target, collectionName);
    }

    @Override
    public Mono<Void> deleteForm(String id) {
        return operations.remove(Query.query(Criteria.where("_id").is(id)), Form.class, collectionName)
                .then();
    }

}
