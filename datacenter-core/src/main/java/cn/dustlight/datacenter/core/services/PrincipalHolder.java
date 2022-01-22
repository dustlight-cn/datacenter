package cn.dustlight.datacenter.core.services;

import cn.dustlight.datacenter.core.entities.DatacenterPrincipal;
import reactor.core.publisher.Mono;

/**
 * 身份信息 Holder
 */
public interface PrincipalHolder {

    Mono<DatacenterPrincipal> getPrincipal();
}
