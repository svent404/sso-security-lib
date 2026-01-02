package it.svent404.security.model.response;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public record UserInfoResponse(
        String username,
        Collection<String> roles
) {
    public static UserInfoResponse from(Authentication auth) {
        return new UserInfoResponse(
                auth.getName(),
                auth.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList()
        );
    }
}

