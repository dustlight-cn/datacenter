package cn.dustlight.datacenter.core.services;

import org.springframework.core.Ordered;
import cn.dustlight.datacenter.core.entities.DatacenterPrincipal;
import cn.dustlight.datacenter.core.entities.forms.Form;
import reactor.core.publisher.Mono;

import java.util.Collection;

/**
 * 表单结构验证器
 * <p>
 * 继承了 Ordered 接口，表示在执行验证时的顺序。
 */
public interface FormValidator extends Ordered {

    /**
     * 验证表单
     *
     * @param forms     待验证记录集合
     * @param principal 当前用户身份
     * @param clientId  应用ID
     * @return 校验完成后的记录集合
     */
    Mono<Collection<Form>> validate(Collection<Form> forms,
                                    DatacenterPrincipal principal,
                                    String clientId);
}
