package plus.datacenter.elasticsearch.services;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.util.StringUtils;
import plus.datacenter.core.DatacenterException;
import plus.datacenter.core.ErrorEnum;
import plus.datacenter.core.entities.forms.Record;
import plus.datacenter.core.services.AbstractRecordService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

@Getter
@Setter
@AllArgsConstructor
public class ElasticsearchRecordService extends AbstractRecordService {

    private ReactiveElasticsearchOperations operations;
    private String indexPrefix;

    @Override
    protected Flux<Record> doInsert(Collection<Record> records) {
        return Flux.fromIterable(records)
                .map(record -> record.getId())
                .collectList()
                .flatMap(ids -> doDelete(ids, records.iterator().next().getClientId()))
                .thenMany(Mono.fromCallable(() -> classify(records))
                        .flatMapMany(stringCollectionMap -> {
                            Iterator<Map.Entry<String, Collection<Record>>> iter = stringCollectionMap.entrySet().iterator();
                            Flux<Record> result = Flux.empty();
                            while (iter.hasNext()) {
                                Map.Entry<String, Collection<Record>> kv = iter.next();
                                IndexCoordinates index = IndexCoordinates.of(kv.getKey());
                                Collection<Record> batchToSave = kv.getValue();
                                result = result.transform(recordFlux -> recordFlux.concatWith(operations.saveAll(batchToSave, index)));
                            }
                            return result;
                        }));
    }

    @Override
    protected Flux<Record> doGet(Collection<String> ids, String clientId) {
        IndexCoordinates index = IndexCoordinates.of(String.format("%s.%s.*", indexPrefix, clientId));
        return operations.multiGet(new NativeSearchQuery(new TermsQueryBuilder("_id", ids)), Record.class, index)
                .filter(recordMultiGetItem -> !recordMultiGetItem.isFailed() && recordMultiGetItem.hasItem())
                .map(recordMultiGetItem -> recordMultiGetItem.getItem());
    }

    @Override
    protected Mono<Void> doUpdate(Collection<String> ids, Record record) {
        return Mono.error(new DatacenterException("Operation 'Update' is not supported"));
    }

    @Override
    protected Mono<Void> doDelete(Collection<String> ids, String clientId) {
        IndexCoordinates index = IndexCoordinates.of(String.format("%s.%s.*", indexPrefix, clientId));
        return operations.delete(new NativeSearchQuery(new TermsQueryBuilder("_id", ids)), Record.class, index)
                .onErrorMap(throwable -> ErrorEnum.DELETE_RESOURCE_FAILED.details(throwable.getMessage()).getException())
                .then();
    }

    protected Map<String, Collection<Record>> classify(Collection<Record> records) {
        if (records == null || records.size() == 0)
            return Collections.emptyMap();
        Map<String, Collection<Record>> result = new HashMap<>();

        for (Record record : records) {
            String key = computeUniqueIndex(record);
            Collection<Record> tmp = result.get(key);
            if (tmp == null)
                result.put(key, (tmp = new HashSet<>()));
            tmp.add(record);
        }

        return result;
    }

    protected String computeUniqueIndex(Record record) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(indexPrefix);
        stringBuilder.append('.');
        stringBuilder.append(record.getClientId());
        stringBuilder.append('.');
        stringBuilder.append(record.getFormName());
        stringBuilder.append('.');
        stringBuilder.append(record.getFormId());

        Map<String, Object> data = record.getData();
        String hash = computeDataTypeHash(data);
        if (StringUtils.hasText(hash)) {
            stringBuilder.append('_');
            stringBuilder.append(hash);
        }
        return stringBuilder.toString();
    }

    private static String computeDataTypeHash(Map<String, Object> data) {
        StringBuilder stringBuilder = new StringBuilder();
        if (data != null && data.size() > 0) {
            Set<String> names = data.keySet();
            String[] arr = new String[names.size()];
            names.toArray(arr);
            Arrays.sort(arr);

            for (String name : arr) {
                Object val;
                if ((val = data.get(name)) instanceof Record) {
                    Record innerRecord = (Record) val;

                    stringBuilder.append(name);
                    stringBuilder.append('-');
                    stringBuilder.append(innerRecord.getFormId());
                } else if (val instanceof Map) {

                    stringBuilder.append(name);
                    stringBuilder.append('-');
                    stringBuilder.append(computeDataTypeHash((Map<String, Object>) val));
                } else if (val != null) {
                    stringBuilder.append(name);
                    stringBuilder.append('-');
                    stringBuilder.append(val.getClass().getName());
                }
            }
        }
        if (stringBuilder.length() > 0)
            return String.valueOf(stringBuilder.toString().hashCode());
        return "";
    }

    @Override
    protected boolean doBeforeCreate() {
        return false;
    }

    @Override
    protected boolean doBeforeUpdate() {
        return false;
    }
}
