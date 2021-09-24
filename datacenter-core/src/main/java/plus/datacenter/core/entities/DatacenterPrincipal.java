package plus.datacenter.core.entities;

import java.security.Principal;
import java.util.Collection;

/**
 * 数据中心身份
 */
public interface DatacenterPrincipal extends Principal {

    /**
     * 获取用户的字符串 UID
     *
     * @return
     */
    String getUidAsString();

    /**
     * 获取用户的 UID
     *
     * @return
     */
    Long getUid();

    /**
     * 获取权限列表
     *
     * @return
     */
    Collection<String> getAuthorities();

    /**
     * 获取访问凭据的作用域
     *
     * @return
     */
    Collection<String> getScopes();

    /**
     * 获取访问凭据的应用 ID
     *
     * @return
     */
    String getClientId();

    /**
     * 判断此访问凭据的用户是否为当前应用的成员
     *
     * @return
     */
    boolean isMember();
}
