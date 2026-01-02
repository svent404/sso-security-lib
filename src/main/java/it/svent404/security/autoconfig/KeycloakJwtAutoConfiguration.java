package it.svent404.security.autoconfig;

import it.svent404.security.service.LocalJwtService;
import it.svent404.security.properties.SsoSecurityProperties;
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
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@ConditionalOnBooleanProperty(prefix = "sso", name = "enabled", havingValue = true, matchIfMissing = false)
@ConditionalOnProperty(prefix = "sso", name = "mode", havingValue = "oauth2", matchIfMissing = false)
@EnableConfigurationProperties(SsoSecurityProperties.class)
@EnableMethodSecurity
public class KeycloakJwtAutoConfiguration {

    @Bean
    public JwtDecoder jwtDecoder(SsoSecurityProperties ssoSecurityProperties) {
        return NimbusJwtDecoder.withJwkSetUri(ssoSecurityProperties.getJwt().getJwkSet()).build();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, LocalJwtService jwtService) {

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/**",
                                "/v3/api-docs/**",
                                "/public/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(grantedAuthoritiesExtractor())
                        )
                )
                .build();
    }

    @Bean
    public JwtAuthenticationConverter grantedAuthoritiesExtractor() {
        JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();

        converter.setAuthoritiesClaimName("realm_access");
        converter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter authConverter = new JwtAuthenticationConverter();
        authConverter.setJwtGrantedAuthoritiesConverter(converter);

        return authConverter;
    }

}
