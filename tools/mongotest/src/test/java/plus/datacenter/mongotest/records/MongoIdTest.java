//package plus.datacenter.mongotest.records;
//
//import com.google.gson.Gson;
//import com.mongodb.client.result.UpdateResult;
//import org.bson.types.ObjectId;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.mongodb.core.ReactiveMongoOperations;
//import org.springframework.data.mongodb.core.query.Criteria;
//import org.springframework.data.mongodb.core.query.Query;
//import org.springframework.data.mongodb.core.query.Update;
//import plus.datacenter.core.entities.forms.Form;
//import plus.datacenter.core.entities.forms.Record;
//
//import java.util.*;
//
///**
// * 转换旧记录中，表单项类型为 FORM 的 ID 值为 ObjectId。
// */
//@SpringBootTest
//public class MongoIdTest {
//
//    @Autowired
//    private ReactiveMongoOperations mongoOperations;
//
//    private static final Gson gson = new Gson();
//
//    @Test
//    public void run() throws Exception {
//
//        List<Record> records = mongoOperations.findAll(Record.class, "form_record")
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
//            UpdateResult result = mongoOperations.updateFirst(Query.query(Criteria.where("_id").is(new ObjectId(record.getId()))),
//                            update, Record.class,
//                            "form_record")
//                    .block();
////            System.out.println(gson.toJson(update.getUpdateObject()));
////            if (!result.wasAcknowledged())
//            System.out.println(gson.toJson(result));
//        });
//
//    }
//
//
//}
