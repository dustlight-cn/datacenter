package plus.datacenter.amqp.sync;

import plus.datacenter.amqp.entities.RecodeEventMessage;
import reactor.core.publisher.Mono;

public interface SyncHandler {

    Mono<Void> sync(RecodeEventMessage eventMessage);

}
