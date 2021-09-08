package plus.datacenter.core.services;

import plus.datacenter.core.entities.DatacenterPrincipal;
import plus.datacenter.core.entities.forms.Record;
import reactor.core.publisher.Mono;

import java.util.Collection;

/**
 * 记录验证器
 */
public interface RecordValidator {

    /**
     * 验证记录
     *
     * @param records   待验证记录集合
     * @param principal 当前用户身份
     * @param clientId  应用ID
     * @return 校验完成后的记录集合
     */
    Mono<Collection<Record>> validate(Collection<Record> records,
                                      DatacenterPrincipal principal,
                                      String clientId);
}
