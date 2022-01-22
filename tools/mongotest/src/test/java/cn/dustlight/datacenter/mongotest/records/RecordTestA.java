//package cn.dustlight.datacenter.mongotest.records;
//
//import com.google.gson.Gson;
//import org.bson.types.ObjectId;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.mongodb.core.ReactiveMongoOperations;
//import org.springframework.data.mongodb.core.aggregation.Aggregation;
//import org.springframework.data.mongodb.core.aggregation.AggregationPipeline;
//import org.springframework.data.mongodb.core.aggregation.LookupOperation;
//import org.springframework.data.mongodb.core.query.Criteria;
//import org.springframework.data.mongodb.core.query.Query;
//import cn.dustlight.datacenter.core.entities.forms.Form;
//import cn.dustlight.datacenter.core.entities.forms.Record;
//import cn.dustlight.datacenter.core.services.EnhancedRecordService;
//import reactor.core.publisher.Mono;
//
//import java.util.*;
//
///**
// * 聚合管道测试
// */
//@SpringBootTest
//public class RecordTestA {
//
//    @Autowired
//    private ReactiveMongoOperations mongoOperations;
//
//    private static final Gson gson = new Gson();
//
//    @Autowired
//    EnhancedRecordService enhancedRecordService;
//
//    @Test
//    public void run() throws Exception {
//
//        String formName = "company";
//        String recordId = "6104f1dc92bd3756ecd4582e";
//
////        System.out.println(gson.toJson(getFormRecord("6104f8f092bd3756ecd45855")));
//        listRecordAboutRecord(formName, recordId)
//                .forEach(record -> {
////                    System.out.println(gson.toJson(record));
//                    System.out.println(getFormRecord(record.getId()));
//                });
//
//        enhancedRecordService.searchAssociatedRecordByIds(Arrays.asList(recordId), "87de67af7030000")
//                .collectList()
//                .flatMapMany(records -> enhancedRecordService.getFullRecords(records, "87de67af7030000"))
//                .collectList()
//                .block()
//                .forEach(record -> System.out.println(record));
//    }
//
//    public Record getFormRecord(String recordId) {
//        Record record = mongoOperations.findOne(Query.query(Criteria.where("id").is(recordId)), Record.class, "form_record")
//                .block();
//        Map<String, Object> data = record.getData();
//        Iterator<Map.Entry<String, Object>> iterator = data.entrySet().iterator();
//        HashSet<String> fields = new HashSet<>();
//        while (iterator.hasNext()) {
//            Map.Entry<String, Object> kv = iterator.next();
//            if (kv.getValue() instanceof ObjectId)
//                fields.add(kv.getKey());
//        }
//        if (fields.size() != 0) {
//            AggregationPipeline pipeline = new AggregationPipeline();
//            pipeline.add(Aggregation.match(Criteria.where("_id").is(recordId)));
//            for (String field : fields) {
//                pipeline.add(LookupOperation.newLookup().from("form_record").localField("data." + field).foreignField("_id").as("data." + field));
//            }
//
//            return mongoOperations.aggregate(Aggregation.newAggregation(pipeline.getOperations()), "form_record", Record.class)
//                    .collectList()
//                    .flatMap(formRecords -> Mono.just(formRecords.get(0))).block();
//        }
//        return record;
//    }
//
//    public List<Record> listRecordAboutRecord(String formName, String recordId) {
//        List<Form> list = mongoOperations.find(Query.query(Criteria.where("groups.items.type").is("FORM").and("groups.items.form").is(formName)), Form.class, "form")
//                .collectList()
//                .block();
//        Collection<Criteria> criteriaCollection = new HashSet<>();
//        for (Form form : list) {
//            Collection<Item> items = form.getItems().values();
//            for (Item item : items) {
//                if (item instanceof FormItem) {
//                    FormItem formItem = (FormItem) item;
//                    if (formName.equals(formItem.getForm())) {
//                        criteriaCollection.add(
//                                Criteria.where("formId").is(form.getId()).and("data." + formItem.getName()).is(new ObjectId(recordId))
//                        );
//                    }
//                }
//            }
//        }
//
//        Query q = Query.query(new Criteria().orOperator(criteriaCollection));
//        List<Record> records = mongoOperations.find(q, Record.class, "form_record").collectList().block();
//        return records;
//    }
//}