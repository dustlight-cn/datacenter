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

import java.util.*;

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
        return operations.find(Query.query(Criteria.where("groups.items.form").in(formNames).and("clientId").is(clientId)), Form.class, formCollection)
                .collectList()
                .flatMapMany(forms -> {
                    Collection<Criteria> criteriaCollection = new HashSet<>();
                    for (Form form : forms) {
//                        Map<String, Item> itemMap = form.getItems();
//                        Iterator<Map.Entry<String, Item>> iter = itemMap.entrySet().iterator();
//
//                        while (iter.hasNext()) {
//                            Map.Entry<String, Item> kv = iter.next();
//                            String itemName = kv.getKey();
//                            Item item = kv.getValue();
//                            if (!(item instanceof FormItem))
//                                continue;
//                            FormItem formItem = (FormItem) item;
//                            if (!formNames.contains(formItem.getForm()))
//                                continue;
//                            criteriaCollection.add(Criteria.where("clientId").is(clientId)
//                                    .and("formId").is(form.getId())
//                                    .and("data." + itemName).in(formNameRecordMap.get(formItem.getForm())));
//                        }
                    }
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
//                    Map<String, Map<String, FormItem>> formIdItemMap = new HashMap<>();

                    Collection<String> fields = new HashSet<>();

                    for (Form form : forms) {
//                        String formId = form.getId();
//                        Map<String, Item> itemMap = form.getItems();
//                        Map<String, FormItem> formItemMap = new HashMap<>();
//
//                        Iterator<Map.Entry<String, Item>> iter = itemMap.entrySet().iterator();
//                        while (iter.hasNext()) {
//                            Map.Entry<String, Item> kv = iter.next();
//                            String itemName = kv.getKey();
//                            Item item = kv.getValue();
//                            if (item instanceof FormItem) {
//                                formItemMap.put(itemName, (FormItem) item);
//                                fields.add(itemName);
//                            }
//                        }
//                        formIdMap.put(formId, form);
//                        formIdItemMap.put(formId, formItemMap);
                    }

                    if (fields.size() == 0)
                        return getRecords(recordIds, clientId);

                    AggregationPipeline pipeline = new AggregationPipeline();
                    pipeline.add(Aggregation.match(Criteria.where("_id").in(recordIds).and("clientId").is(clientId)));
                    for (String field : fields) {
                        pipeline.add(LookupOperation.newLookup()
                                .from("form_record")
                                .localField("data." + field)
                                .foreignField("_id")
                                .as("_data." + field));
                    }
                    return operations.aggregate(Aggregation.newAggregation(pipeline.getOperations()), recordCollection, EnhancedRecord.class)
                            .map(enhancedRecord -> {
                                Map<String, Object> data = enhancedRecord.getData();
                                Map<String, Collection<Record>> _data = enhancedRecord.get_data();
//                                Map<String, FormItem> itemMap = formIdItemMap.get(enhancedRecord.getFormId());
//
//                                if (_data != null && itemMap != null && itemMap.size() > 0 && _data.size() > 0) {
//                                    Iterator<Map.Entry<String, FormItem>> iter = itemMap.entrySet().iterator();
//                                    while (iter.hasNext()) {
//                                        Map.Entry<String, FormItem> kv = iter.next();
//                                        String itemName = kv.getKey();
//                                        FormItem formItem = kv.getValue();
//                                        Object value = null;
//                                        if (_data.containsKey(itemName) && (value = _data.get(itemName)) != null) {
//                                            if (formItem.getArray() != null && formItem.getArray())
//                                                data.put(itemName, value);
//                                            else {
//                                                Collection arrVal = (Collection) value;
//                                                if (arrVal.size() == 0)
//                                                    data.put(itemName, null);
//                                                else
//                                                    data.put(itemName, arrVal.iterator().next());
//                                            }
//                                        } else {
//                                            data.put(itemName, null);
//                                        }
//                                    }
//                                }
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


}
