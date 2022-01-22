package cn.dustlight.datacenter.amqp.sync;

import cn.dustlight.datacenter.amqp.entities.RecodeEvent;
import reactor.core.publisher.Mono;

public interface SyncHandler {

    Mono<Void> sync(RecodeEvent eventMessage);

}
