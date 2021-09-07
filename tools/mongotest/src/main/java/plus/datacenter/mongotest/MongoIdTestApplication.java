//package plus.datacenter.mongotest;
//
//import com.google.gson.Gson;
//import com.mongodb.client.result.UpdateResult;
//import com.mongodb.reactivestreams.client.MongoClient;
//import org.bson.types.ObjectId;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.data.mongodb.core.ReactiveMongoOperations;
//import org.springframework.data.mongodb.core.query.Criteria;
//import org.springframework.data.mongodb.core.query.Query;
//import org.springframework.data.mongodb.core.query.Update;
//import org.springframework.stereotype.Component;
//import plus.datacenter.core.entities.forms.Form;
//import plus.datacenter.core.entities.forms.FormRecord;
//import plus.datacenter.core.entities.forms.Item;
//import plus.datacenter.core.entities.forms.items.FormItem;
//import java.util.*;
//
//@Component
//@SpringBootApplication
//public class MongoIdTestApplication implements ApplicationRunner {
//
//    @Autowired
//    private ReactiveMongoOperations mongoOperations;
//
//    private static final Gson gson = new Gson();
//
//    @Autowired
//    private MongoClient mongoClient;
//
//    public static void main(String[] args) {
//        SpringApplication.run(MongoIdTestApplication.class, args);
//    }
//
//    @Override
//    public void run(ApplicationArguments args) throws Exception {
//
//        List<FormRecord> records = mongoOperations.findAll(FormRecord.class, "form_record")
//                .collectList().block();
//
//        records.forEach(record -> {
//            Form form = mongoOperations.findById(record.getFormId(), Form.class, "form")
//                    .block();
//            Map<String, Item> items = form.getItems();
//            Map<String, Object> data = record.getData();
//
//            Iterator<Map.Entry<String, Item>> iterator = items.entrySet().iterator();
//            Update update = new Update();
//            while (iterator.hasNext()) {
//                Map.Entry<String, Item> kv = iterator.next();
//                if (kv.getValue() instanceof FormItem) {
//
//                    try {
//                        Object tmp = data.get(kv.getKey());
//                        if (tmp == null)
//                            continue;
//                        if (tmp instanceof Collection) {
//                            Collection<ObjectId> objectIds = new HashSet<>();
//                            Collection coll = (Collection) tmp;
//                            for (Object tmp2 : coll) {
//                                if (tmp2 == null)
//                                    continue;
//                                if (tmp2 instanceof ObjectId)
//                                    objectIds.add((ObjectId) tmp2);
//                                objectIds.add(new ObjectId(tmp2.toString()));
//                            }
//                            data.put(kv.getKey(), objectIds);
//                        } else {
//                            if (tmp instanceof ObjectId)
//                                continue;
//                            data.put(kv.getKey(), new ObjectId(tmp.toString()));
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        System.err.println(gson.toJson(record));
////                        e.printStackTrace();
//                    }
//                }
//            }
//            update.set("data", data);
////            update.getUpdateObject();
//            UpdateResult result = mongoOperations.updateFirst(Query.query(Criteria.where("_id").is(new ObjectId(record.getId()))), update, FormRecord.class,"form_record")
//                    .block();
////            System.out.println(gson.toJson(update.getUpdateObject()));
////            if (!result.wasAcknowledged())
//                System.out.println(gson.toJson(result));
//        });
//
//    }
//
//
//}
