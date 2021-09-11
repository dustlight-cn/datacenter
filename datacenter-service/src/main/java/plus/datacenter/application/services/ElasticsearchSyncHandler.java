package plus.datacenter.application.services;

import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;
import plus.datacenter.amqp.entities.RecodeEvent;
import plus.datacenter.amqp.sync.SyncHandler;
import plus.datacenter.core.entities.forms.Record;
import plus.datacenter.core.services.EnhancedRecordService;
import plus.datacenter.elasticsearch.services.ElasticsearchRecordService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

@Getter
@Setter
@AllArgsConstructor
public class ElasticsearchSyncHandler implements SyncHandler {

    private EnhancedRecordService enhancedRecordService;
    private ElasticsearchRecordService elasticsearchRecordService;

    private static final Gson gson = Converters.registerInstant(new GsonBuilder()).create();

    private static final Log logger = LogFactory.getLog(ElasticsearchSyncHandler.class);

    @Override
    public Mono<Void> sync(RecodeEvent eventMessage) {
        Collection<Record> records = eventMessage.getRecords();
        String clientID = records.iterator().next().getClientId();

        switch (eventMessage.getType()) {
            case DELETE:
                return Flux.fromIterable(records)
                        .map(recordz -> recordz.getId())
                        .filter(id -> StringUtils.hasText(id))
                        .collectList()
                        .flatMap(ids -> elasticsearchRecordService.deleteRecords(ids, clientID))
                        .then(enhancedRecordService.searchAssociatedRecords(records, clientID)
                                .collectList()
                                .flatMapMany(targets -> enhancedRecordService.getFullRecords(targets, clientID))
                                .collectList()
                                .flatMapMany(recordz -> elasticsearchRecordService.createRecords(recordz, clientID))
                                .then());
            case UPDATE:
            case CREATE:
            default:
                return enhancedRecordService.searchAssociatedRecords(records, clientID)
                        .concatWith(Flux.fromIterable(records))
                        .collectList()
                        .flatMapMany(targets -> enhancedRecordService.getFullRecords(targets, clientID))
                        .collectList()
                        .flatMapMany(recordz -> elasticsearchRecordService.createRecords(recordz, clientID))
                        .then();
        }
    }
}
