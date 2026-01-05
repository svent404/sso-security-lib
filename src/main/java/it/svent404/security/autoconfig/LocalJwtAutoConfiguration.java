package it.svent404.security.autoconfig;

import it.svent404.security.controller.LocalJwtController;
import it.svent404.security.filter.LocalJwtAuthFilter;
import it.svent404.security.repository.InMemoryTokenRepository;
import it.svent404.security.service.LocalJwtService;
import it.svent404.security.properties.SsoSecurityProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.time.Clock;

@Configuration
@ConditionalOnBooleanProperty(prefix = "sso", name = "enabled", havingValue = true, matchIfMissing = false)
@ConditionalOnProperty(prefix = "sso", name = "mode", havingValue = "local", matchIfMissing = false)
@EnableConfigurationProperties(SsoSecurityProperties.class)
@EnableMethodSecurity
@Import(LocalJwtController.class)
public class LocalJwtAutoConfiguration {

    @Bean
    LocalJwtService localJwtService(
            SsoSecurityProperties props,
            Clock clock,
            InMemoryTokenRepository tokenRepository) {


        return new LocalJwtService(props, clock, tokenRepository);
    }

    @Bean
    InMemoryTokenRepository inMemoryTokenRepository() {
        return new InMemoryTokenRepository();
    }

    @Bean
    @ConditionalOnMissingBean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    @ConditionalOnMissingBean
    LocalJwtAuthFilter jwtAuthenticationFilter(
                LocalJwtService jwtService){
        return new LocalJwtAuthFilter(jwtService);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, LocalJwtService jwtService) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/webjars/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new LocalJwtAuthFilter(jwtService),
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {

        UserDetails admin = User.withUsername("admin")
                .password(encoder.encode("admin"))
                .roles("ADMIN")
                .build();

        UserDetails user = User.withUsername("user")
                .password(encoder.encode("user"))
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(admin, user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}

