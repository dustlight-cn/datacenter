package plus.datacenter.amqptest;

import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import plus.datacenter.amqp.entities.RecodeEventMessage;
import plus.datacenter.amqp.sync.SyncHandler;
import plus.datacenter.core.DatacenterException;
import plus.datacenter.core.entities.forms.Record;
import plus.datacenter.core.services.EnhancedRecordService;
import reactor.core.publisher.Mono;

import java.util.Collection;

@Component
public class MySyncHandler implements SyncHandler {

    private static final Gson gson = Converters.registerInstant(new GsonBuilder()).create();

    Log logger = LogFactory.getLog(getClass());

    @Autowired
    EnhancedRecordService enhancedRecordService;

    @Override
    public Mono<Void> sync(RecodeEventMessage event) {
        Collection<Record> records = event.getRecords();
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
