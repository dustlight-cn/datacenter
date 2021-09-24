package plus.datacenter.mongo.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationPipeline;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.StringUtils;
import plus.datacenter.core.DatacenterException;
import plus.datacenter.core.entities.forms.Form;
import plus.datacenter.core.entities.forms.Record;
import plus.datacenter.core.services.EnhancedRecordService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.Function;

@Getter
@Setter
@AllArgsConstructor
public class MongoEnhancedRecordService implements EnhancedRecordService {

    private ReactiveMongoOperations operations;

    /**
     * 记录集合名称
     */
    private String recordCollection;
    /**
     * 表单集合名称
     */
    private String formCollection;

    @Override
    public Flux<Record> searchAssociatedRecords(Collection<Record> records, String clientId) {
        if (records == null || records.size() == 0)
            return Flux.empty();
        Map<String, Collection<ObjectId>> formNameRecordMap = new HashMap<>();
        for (Record record : records) {
            if (record == null || !StringUtils.hasText(record.getFormName()))
                continue;
            Collection<ObjectId> recordCollection = formNameRecordMap.get(record.getFormName());
            if (recordCollection == null)
                formNameRecordMap.put(record.getFormName(), (recordCollection = new HashSet<>()));
            recordCollection.add(new ObjectId(record.getId()));
        }
        if (formNameRecordMap.size() == 0)
            return Flux.empty();

        Set<String> formNames = formNameRecordMap.keySet();
        return operations.find(Query.query(Criteria.where("references").in(formNames).and("clientId").is(clientId)), Form.class, formCollection)
                .collectList()
                .flatMapMany(forms -> {
                    Collection<Criteria> criteriaCollection = new HashSet<>();
                    for (Form form : forms) {
                        Map<String, String> referenceMap = form.getReferenceMap();
                        for (var kv : referenceMap.entrySet()) {
                            var refForm = kv.getValue();
                            if (!formNames.contains(refForm))
                                continue;
                            criteriaCollection.add(Criteria.where("clientId").is(clientId)
                                    .and("formId").is(form.getId())
                                    .and("data." + kv.getKey().replace('/', '.')).in(formNameRecordMap.get(refForm)));
                        }
                    }
                    if (criteriaCollection == null || criteriaCollection.size() == 0)
                        return Mono.empty();
                    return operations.find(Query.query(new Criteria().orOperator(criteriaCollection)), Record.class, recordCollection);
                });
    }

    @Override
    public Flux<Record> searchAssociatedRecordByIds(Collection<String> recordIds, String clientId) {
        if (recordIds == null || recordIds.size() == 0)
            return Flux.empty();
        return getRecords(recordIds, clientId)
                .collectList()
                .flatMapMany(records -> searchAssociatedRecords(records, clientId));
    }

    @Override
    public Flux<Record> getFullRecords(Collection<Record> records, String clientId) {
        if (records == null || records.size() == 0)
            return Flux.empty();
        Collection<String> formIds = new HashSet<>();
        Collection<String> recordIds = new HashSet<>();
        for (Record record : records) {
            if (record == null
                    || !StringUtils.hasText(record.getFormId())
                    || !StringUtils.hasText(record.getId()))
                continue;
            formIds.add(record.getFormId());
            recordIds.add(record.getId());
        }
        return operations.find(Query.query(Criteria.where("_id").in(formIds).and("clientId").is(clientId)), Form.class, formCollection)
                .collectList()
                .flatMapMany(forms -> {
                    Map<String, Form> formIdMap = new HashMap<>();

                    Collection<String> fields = new HashSet<>();

                    for (Form form : forms) {
                        if (form.getReferenceMap() != null)
                            fields.addAll(form.getReferenceMap().keySet());
                        formIdMap.put(form.getId(), form);
                    }

                    if (fields.size() == 0)
                        return getRecords(recordIds, clientId);

                    AggregationPipeline pipeline = new AggregationPipeline();
                    pipeline.add(Aggregation.match(Criteria.where("_id").in(recordIds).and("clientId").is(clientId)));
                    for (String field : fields) {
                        field = field.replace('/', '.');
                        pipeline.add(LookupOperation.newLookup()
                                .from(recordCollection)
                                .localField("data." + field)
                                .foreignField("_id")
                                .as("_data." + field));
                    }
                    return operations.aggregate(Aggregation.newAggregation(pipeline.getOperations()), recordCollection, EnhancedRecord.class)
                            .map(enhancedRecord -> {
                                Map<String, Object> data = enhancedRecord.getData();
                                Map<String, Collection<Record>> _data = enhancedRecord.get_data();
                                Form form = formIdMap.get(enhancedRecord.getFormId());
                                if (form != null && form.getReferenceMap() != null) {
                                    Map<String, String> referenceMap = form.getReferenceMap();
                                    for (var kv : referenceMap.entrySet()) {
                                        String path = kv.getKey();
                                        Object newVal = getMapItem(path, _data, 0);
                                        putMapItem(path, data, 0, (obj) -> {
                                            if (newVal == null)
                                                return null;
                                            if (obj == null)
                                                return newVal;
                                            if (obj instanceof Collection || obj.getClass().isArray()) {
                                                return (newVal instanceof Collection) ? newVal : Arrays.asList(newVal);
                                            } else {
                                                if (newVal instanceof Collection) {
                                                    Iterator<?> iter = ((Collection<?>) newVal).iterator();
                                                    if (iter != null && iter.hasNext())
                                                        return iter.next();
                                                    return null;
                                                } else
                                                    return newVal;
                                            }
                                        });
                                    }
                                }
                                try {
                                    return enhancedRecord.castToRecord();
                                } catch (CloneNotSupportedException e) {
                                    throw new DatacenterException("Fail to cast EnhancedRecord to Record", e);
                                }
                            });
                });
    }

    @Override
    public Flux<Record> getFullRecordByIds(Collection<String> ids, String clientId) {
        return getRecords(ids, clientId)
                .collectList()
                .flatMapMany(records -> getFullRecords(records, clientId));
    }

    protected Flux<Record> getRecords(Collection<String> recordIds, String clientId) {
        return operations.find(Query.query(Criteria.where("_id").in(recordIds).and("clientId").is(clientId)), Record.class, recordCollection);
    }

    @Getter
    @Setter
    public static class EnhancedRecord extends Record {

        @JsonIgnore
        private transient Map<String, Collection<Record>> _data;

    }

    private static Object getMapItem(String path, Object target, int pathIndex) {
        if (target == null)
            return null;
        if (pathIndex >= path.length()) {
            return target;
        } else {
            if (!(target instanceof Map))
                return null;
            Map m = (Map) target;
            int newIndex = path.indexOf('/', pathIndex);
            if (newIndex == -1)
                newIndex = path.length();
            String key = path.substring(pathIndex, newIndex);
            return getMapItem(path, m.get(key), newIndex + 1);
        }
    }

    private void putMapItem(String path, Map target, int pathIndex, Function<Object, Object> handler) {
        if (target == null)
            return;
        int index = path.indexOf('/', pathIndex);
        int nextIndex = index == -1 ? path.length() : index;
        String key = path.substring(pathIndex, nextIndex);
        if (index == -1) {
            target.put(key, handler.apply(target.get(key)));
        } else {
            Object nextTarget = target.get(key);
            if (!(nextTarget instanceof Map)) {
                Map newTarget = new HashMap();
                target.put(key, newTarget);
                nextTarget = newTarget;
            }
            putMapItem(path, (Map) nextTarget, nextIndex + 1, handler);
        }

    }
}
