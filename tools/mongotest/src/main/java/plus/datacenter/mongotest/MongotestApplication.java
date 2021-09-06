package plus.datacenter.mongotest;

import com.google.gson.Gson;
import com.mongodb.reactivestreams.client.MongoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import plus.datacenter.core.entities.forms.Form;
import plus.datacenter.core.entities.forms.FormRecord;
import plus.datacenter.core.entities.forms.Item;
import plus.datacenter.core.entities.forms.items.FormItem;

import java.util.*;

@Component
@SpringBootApplication
public class MongotestApplication implements ApplicationRunner {

    @Autowired
    private ReactiveMongoOperations mongoOperations;

    private static final Gson gson = new Gson();

    @Autowired
    private MongoClient mongoClient;

    public static void main(String[] args) {
        SpringApplication.run(MongotestApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        String formName = "company";
        String recordId = "6104f1dc92bd3756ecd4582e";

        listRecordAboutRecord(formName, recordId)
                .forEach(record -> System.out.println(gson.toJson(record)));

    }

    public List<FormRecord> listRecordAboutRecord(String formName, String recordId) {
        List<Form> list = mongoOperations.find(Query.query(Criteria.where("groups.items.type").is("FORM").and("groups.items.form").is(formName)), Form.class, "form")
                .collectList()
                .block();
        Collection<Criteria> criteriaCollection = new HashSet<>();
        for (Form form : list) {
            Collection<Item> items = form.getItems().values();
            for (Item item : items) {
                if (item instanceof FormItem) {
                    FormItem formItem = (FormItem) item;
                    if (formName.equals(formItem.getForm())) {
                        criteriaCollection.add(
                                Criteria.where("formId").is(form.getId()).and("data." + formItem.getName()).is(recordId)
                        );
                    }
                }
            }
        }

        Query q = Query.query(new Criteria().orOperator(criteriaCollection));
        List<FormRecord> records = mongoOperations.find(q, FormRecord.class, "form_record").collectList().block();
        return records;
    }
}
