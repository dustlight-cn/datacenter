package cn.dustlight.datacenter.application.services;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;
import cn.dustlight.auth.resources.AuthPrincipalUtil;
import cn.dustlight.auth.resources.core.AuthPrincipal;
import cn.dustlight.datacenter.core.ErrorEnum;
import cn.dustlight.datacenter.core.entities.DatacenterPrincipal;
import cn.dustlight.datacenter.core.services.PrincipalHolder;
import reactor.core.publisher.Mono;

import java.util.Collection;

public class DefaultPrincipalHolder implements PrincipalHolder {

    @Override
    public Mono<DatacenterPrincipal> getPrincipal() {
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(securityContext -> securityContext.getAuthentication() instanceof AbstractOAuth2TokenAuthenticationToken ?
                        Mono.just((AbstractOAuth2TokenAuthenticationToken) securityContext.getAuthentication()) :
                        Mono.error(ErrorEnum.ACCESS_DENIED.details("Principal is not OAuth2 token").getException()))
                .map(token -> AuthPrincipalUtil.getAuthPrincipal(token))
                .switchIfEmpty(Mono.error(ErrorEnum.ACCESS_DENIED.details("Principal is null").getException()))
                .map(principal -> new DefaultDatacenterPrincipal(principal));
    }

    public static class DefaultDatacenterPrincipal implements DatacenterPrincipal {

        private AuthPrincipal authPrincipal;

        public DefaultDatacenterPrincipal(AuthPrincipal authPrincipal) {
            this.authPrincipal = authPrincipal;
        }

        @Override
        public String getUidAsString() {
            return authPrincipal.getUidString();
        }

        @Override
        public Long getUid() {
            return authPrincipal.getUid();
        }

        @Override
        public Collection<String> getAuthorities() {
            return authPrincipal.getAuthorities();
        }

        @Override
        public Collection<String> getScopes() {
            return authPrincipal.getScope();
        }

        @Override
        public String getClientId() {
            return authPrincipal.getClientId();
        }

        @Override
        public boolean isMember() {
            return authPrincipal.isMember();
        }

        @Override
        public String getName() {
            return authPrincipal.getName();
        }
    }
}
