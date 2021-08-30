package plus.datacenter.es2mongo;

import com.mongodb.reactivestreams.client.MongoClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.stereotype.Component;
import plus.datacenter.core.entities.forms.Form;
import plus.datacenter.core.entities.forms.FormRecord;
import reactor.core.publisher.Mono;

import java.util.LinkedList;

@Component
@SpringBootApplication
public class Es2MongoApplication implements ApplicationRunner {

    @Autowired
    private ReactiveElasticsearchOperations elasticsearchOperations;

    @Autowired
    private ReactiveMongoOperations mongoOperations;

    @Autowired
    private MongoClient mongoClient;

    public static void main(String[] args) {
        SpringApplication.run(Es2MongoApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String formMetaIndex = "datacenter.form_meta";
        String formMetaCollection = "form_meta";
        String formIndex = "datacenter.form";
        String formCollection = "form";
        String formRecordIndex = "datacenter.form_record.*";
        String formRecordCollection = "form_record";
        Log logger = LogFactory.getLog("Es2Mongo");

        logger.info("Start.");
        Mono.from(mongoClient.startSession())
                .map(clientSession -> {
                    logger.info("Starting client session.");
                    clientSession.startTransaction();
                    return clientSession;
                })
                .flatMap(clientSession -> elasticsearchOperations.searchForPage(Query.findAll()
                                        .setPageable(Pageable.ofSize(1000).withPage(0)),
                                FormMeta.class,
                                IndexCoordinates.of(formMetaIndex))
                        .flatMap(searchHits -> {
                            LinkedList<FormMeta> metas = new LinkedList<>();
                            searchHits.forEach(formMetaSearchHit -> metas.add(formMetaSearchHit.getContent()));
                            logger.info(String.format("Count of FormMeta: %d", metas.size()));
                            return mongoOperations.withSession(clientSession)
                                    .insert(metas, formMetaCollection)
                                    .collectList();
                        })
                        .flatMap(formMetas -> elasticsearchOperations.searchForPage(Query.findAll()
                                        .setPageable(Pageable.ofSize(1000).withPage(0)),
                                Form.class,
                                IndexCoordinates.of(formIndex)))
                        .flatMap(searchHits -> {
                            LinkedList<Form> forms = new LinkedList<>();
                            searchHits.forEach(formMetaSearchHit -> forms.add(formMetaSearchHit.getContent()));

                            logger.info(String.format("Count of Form: %d", forms.size()));
                            return mongoOperations.withSession(clientSession)
                                    .insert(forms, formCollection)
                                    .collectList();
                        })
                        .map(formMetas -> {
                            int page = 0;
                            long total;
                            long count = 0;
                            LinkedList<FormRecord> formRecords = new LinkedList<>();
                            while (true) {
                                SearchPage<FormRecord> hits = elasticsearchOperations.searchForPage(Query.findAll()
                                                .setPageable(Pageable.ofSize(100).withPage(page++)),
                                        FormRecord.class,
                                        IndexCoordinates.of(formRecordIndex)).block();

                                hits.forEach(formMetaSearchHit -> formRecords.add(formMetaSearchHit.getContent()));

                                count += hits.getContent().size();
                                total = hits.getTotalElements();
                                if(count >= total)
                                    break;
                            }
                            return formRecords;

                        })
                        .flatMap(formRecords -> {

                            logger.info(String.format("Count of FormRecord: %d", formRecords.size()));

                            return mongoOperations.withSession(clientSession)
                                    .insert(formRecords, formRecordCollection)
                                    .collectList();
                        })
                        .flatMap(obj -> {
                            logger.info("Success.");
                            return Mono.from(clientSession.commitTransaction()).then(Mono.just(obj));
                        })
                        .onErrorResume(throwable -> {
                            logger.error(String.format("Error: %s", throwable.getMessage()), throwable);
                            return Mono.from(clientSession.abortTransaction()).then(Mono.error(throwable));
                        })
                        .doFinally(signalType -> {
                            logger.info("Closing client session.");
                            clientSession.close();
                            logger.info("Done.");
                        })
                ).block();
    }
}
