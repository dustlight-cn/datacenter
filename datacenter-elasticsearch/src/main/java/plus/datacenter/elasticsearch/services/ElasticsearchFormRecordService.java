package plus.datacenter.elasticsearch.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.data.elasticsearch.core.query.UpdateResponse;
import plus.datacenter.core.ErrorEnum;
import plus.datacenter.core.entities.forms.FormRecord;
import plus.datacenter.core.services.FormRecordService;
import reactor.core.publisher.Mono;

@Getter
@Setter
@AllArgsConstructor
public class ElasticsearchFormRecordService implements FormRecordService {

    private static ObjectMapper mapper = new ObjectMapper();
    private ReactiveElasticsearchOperations operations;
    private String indexPrefix;

    @Override
    public Mono<FormRecord> createRecord(FormRecord origin) {
        IndexCoordinates index = IndexCoordinates.of(indexPrefix + "." + origin.getClientId() + "." + origin.getFormName() + "." + origin.getFormId());
        return operations.save(origin, index)
                .onErrorMap(throwable -> ErrorEnum.CREATE_RESOURCE_FAILED.details(throwable.getMessage()).getException());
    }

    @Override
    public Mono<FormRecord> getRecord(String id) {
        IndexCoordinates index = IndexCoordinates.of(indexPrefix + "*");
        return operations.get(id, FormRecord.class, index)
                .switchIfEmpty(Mono.error(ErrorEnum.RESOURCE_NOT_FOUND.getException()));
    }

    @Override
    public Mono<FormRecord> updateRecord(FormRecord target) {
        try {
            IndexCoordinates index = IndexCoordinates.of(indexPrefix + "." + target.getClientId() + "." + target.getFormName() + "." + target.getFormId());
            UpdateQuery updateQuery = UpdateQuery.builder(target.getId()).withDocument(Document.parse(mapper.writeValueAsString(target))).build();
            return operations.update(updateQuery, index)
                    .onErrorMap(throwable -> ErrorEnum.UPDATE_RESOURCE_FAILED.details(throwable.getMessage()).getException())
                    .flatMap(updateResponse -> updateResponse.getResult() == UpdateResponse.Result.UPDATED ? Mono.just(target) : Mono.error(ErrorEnum.UPDATE_RESOURCE_FAILED.getException()));
        } catch (JsonProcessingException e) {
            throw ErrorEnum.UPDATE_RESOURCE_FAILED.details(e.getMessage()).getException();
        }
    }

    @Override
    public Mono<Void> deleteRecord(String id) {
        IndexCoordinates index = IndexCoordinates.of(indexPrefix + "*");
        return operations.delete(new NativeSearchQuery(new MatchQueryBuilder("_id", id)), FormRecord.class, index)
                .onErrorMap(throwable -> ErrorEnum.DELETE_RESOURCE_FAILED.details(throwable.getMessage()).getException())
                .flatMap(byQueryResponse -> Mono.empty());
    }
}
