package plus.datacenter.amqp.sync;

import plus.datacenter.amqp.entities.RecodeEvent;
import reactor.core.publisher.Mono;

public interface SyncHandler {

    Mono<Void> sync(RecodeEvent eventMessage);

}
