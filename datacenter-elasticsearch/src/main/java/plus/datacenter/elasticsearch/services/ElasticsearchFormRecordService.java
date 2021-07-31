package plus.datacenter.elasticsearch.services;

import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.*;
import plus.datacenter.core.ErrorEnum;
import plus.datacenter.core.entities.forms.FormRecord;
import plus.datacenter.core.services.FormRecordService;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class ElasticsearchFormRecordService implements FormRecordService {

    private static final Gson gson = Converters.registerInstant(new GsonBuilder()).create();
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
        IndexCoordinates index = IndexCoordinates.of(indexPrefix + "." + target.getClientId() + "." + target.getFormName() + "." + target.getFormId());
        Map<String, Object> update = new HashMap<>();
        Map<String, Object> data = new HashMap<>();

        update.put("createdAt", target.getCreatedAt());
        update.put("updatedAt", target.getUpdatedAt());
        update.put("data", data);
        Iterator<Map.Entry<String, Object>> iterator = target.getData().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> kv = iterator.next();
            if (kv.getValue() == null)
                continue;
            data.put(kv.getKey(), kv.getValue());
        }
        Document doc = Document.parse(gson.toJson(update));
        doc.setIndex(index.getIndexName());
        doc.setId(target.getId());
        return operations.update(UpdateQuery.builder(target.getId()).withDocument(doc).build(), index)
                .onErrorMap(throwable -> ErrorEnum.UPDATE_RESOURCE_FAILED.details(throwable.getMessage()).getException())
                .flatMap(updateResponse -> updateResponse.getResult() == UpdateResponse.Result.UPDATED ?
                        Mono.just(target) : Mono.error(ErrorEnum.UPDATE_RESOURCE_FAILED.details(updateResponse.getResult().name()).getException()));
    }

    @Override
    public Mono<Void> deleteRecord(String id) {
        IndexCoordinates index = IndexCoordinates.of(indexPrefix + "*");
        return operations.delete(new NativeSearchQuery(new MatchQueryBuilder("_id", id)), FormRecord.class, index)
                .onErrorMap(throwable -> ErrorEnum.DELETE_RESOURCE_FAILED.details(throwable.getMessage()).getException())
                .flatMap(byQueryResponse -> Mono.empty());
    }

    @Override
    public Mono<Void> deleteRecords(Collection<String> ids) {
        IndexCoordinates index = IndexCoordinates.of(indexPrefix + "*");
        return operations.delete(new NativeSearchQuery(new TermsQueryBuilder("_id", ids)), FormRecord.class, index)
                .onErrorMap(throwable -> ErrorEnum.DELETE_RESOURCE_FAILED.details(throwable.getMessage()).getException())
                .flatMap(byQueryResponse -> Mono.empty());
    }
}
