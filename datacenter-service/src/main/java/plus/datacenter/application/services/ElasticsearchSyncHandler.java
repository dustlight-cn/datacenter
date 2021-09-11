package plus.datacenter.application.services;

import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import plus.datacenter.amqp.entities.RecodeEvent;
import plus.datacenter.amqp.sync.SyncHandler;
import plus.datacenter.core.DatacenterException;
import plus.datacenter.core.entities.forms.Record;
import plus.datacenter.core.services.EnhancedRecordService;
import reactor.core.publisher.Mono;

import java.util.Collection;

@Getter
@Setter
@AllArgsConstructor
public class ElasticsearchSyncHandler implements SyncHandler {

    private EnhancedRecordService enhancedRecordService;

    private static final Gson gson = Converters.registerInstant(new GsonBuilder()).create();

    private static final Log logger = LogFactory.getLog(ElasticsearchSyncHandler.class);

    @Override
    public Mono<Void> sync(RecodeEvent eventMessage) {
        Collection<Record> records = eventMessage.getRecords();
        String clientID = records.iterator().next().getClientId();

        return enhancedRecordService.searchAssociatedRecords(records, clientID)
                .collectList()
                .flatMapMany(targets -> enhancedRecordService.getFullRecords(targets, clientID))
                .collectList()
                .flatMap(recordz -> {
                    logger.info(gson.toJson(recordz));
                    return Mono.error(new DatacenterException("😀⭐🐒🐕😄"));
                })
                .then();
    }
}
