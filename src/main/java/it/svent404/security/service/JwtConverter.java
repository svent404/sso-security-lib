package it.svent404.security.service;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter scopeConverter;
    private final String principalAttribute;
    private final String resourceId;

    public JwtConverter(String principalAttribute, String resourceId) {
        this.principalAttribute = principalAttribute;
        this.resourceId = resourceId;

        this.scopeConverter = new JwtGrantedAuthoritiesConverter();
        this.scopeConverter.setAuthorityPrefix("SCOPE_");
        this.scopeConverter.setAuthoritiesClaimName("scope");
    }

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {

        Set<GrantedAuthority> authorities = Stream.of(
                        scopeConverter.convert(jwt),
                        extractRealmRoles(jwt),
                        extractClientRoles(jwt)
                )
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        return new JwtAuthenticationToken(
                jwt,
                authorities,
                getPrincipalName(jwt)
        );
    }

    private String getPrincipalName(Jwt jwt) {
        String claim = principalAttribute != null
                ? principalAttribute
                : JwtClaimNames.SUB;

        return jwt.getClaimAsString(claim);
    }

    private Collection<GrantedAuthority> extractRealmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null) {
            return Set.of();
        }

        Object roles = realmAccess.get("roles");
        if (!(roles instanceof Collection<?> roleList)) {
            return Set.of();
        }

        return roleList.stream()
                .map(Object::toString)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toSet());
    }

    private Collection<GrantedAuthority> extractClientRoles(Jwt jwt) {
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess == null || !resourceAccess.containsKey(resourceId)) {
            return Set.of();
        }

        Object resource = resourceAccess.get(resourceId);
        if (!(resource instanceof Map<?, ?> resourceMap)) {
            return Set.of();
        }

        Object roles = resourceMap.get("roles");
        if (!(roles instanceof Collection<?> roleList)) {
            return Set.of();
        }

        return roleList.stream()
                .map(Object::toString)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toSet());
    }
}