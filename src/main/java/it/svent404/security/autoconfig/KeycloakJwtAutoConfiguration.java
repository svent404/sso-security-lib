package it.svent404.security.autoconfig;

import it.svent404.security.service.JwtConverter;
import it.svent404.security.properties.SsoSecurityProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@ConditionalOnBooleanProperty(prefix = "sso", name = "enabled", havingValue = true, matchIfMissing = false)
@ConditionalOnProperty(prefix = "sso", name = "mode", havingValue = "oauth2", matchIfMissing = false)
@EnableConfigurationProperties(SsoSecurityProperties.class)
@EnableMethodSecurity
public class KeycloakJwtAutoConfiguration {

    @Bean
    public JwtDecoder jwtDecoder(@Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") String jwtSetUri) {
        return NimbusJwtDecoder.withJwkSetUri(jwtSetUri).build();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            JwtConverter jwtConverter) {

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/webjars/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtConverter)
                        )
                )
                .build();
    }

    @Bean
    public JwtConverter jwtConverter(SsoSecurityProperties ssoSecurityProperties) {
        return new JwtConverter(ssoSecurityProperties.getJwt().getAuth().getConverter().getPrincipleAttribute(),
                ssoSecurityProperties.getJwt().getAuth().getConverter().getResourceId());
    }

}
