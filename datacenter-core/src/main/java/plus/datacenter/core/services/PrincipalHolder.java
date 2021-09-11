package plus.datacenter.core.services;

import plus.datacenter.core.entities.DatacenterPrincipal;
import reactor.core.publisher.Mono;

/**
 * 身份信息 Holder
 */
public interface PrincipalHolder {

    Mono<DatacenterPrincipal> getPrincipal();
}
