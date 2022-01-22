package plus.datacenter.application.configurations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import cn.dustlight.auth.resources.AuthSecurityWebFilterChainConfiguration;
import plus.datacenter.schema.resources.SchemaResourceProperties;

@EnableReactiveMethodSecurity
@Configuration
public class SecurityConfig extends AuthSecurityWebFilterChainConfiguration {

    @Autowired
    private SchemaResourceProperties schemaResourceProperties;

    @Override
    protected ServerHttpSecurity configure(ServerHttpSecurity http) {
        return http.authorizeExchange()
                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .pathMatchers(HttpMethod.GET, schemaResourceProperties.getMapping()).permitAll()
                .pathMatchers("/v*/**").authenticated()
                .anyExchange().permitAll()
                .and();
    }
}
