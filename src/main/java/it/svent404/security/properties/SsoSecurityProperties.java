package it.svent404.security.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sso")
@Getter @Setter
public class SsoSecurityProperties {
    private boolean enabled = true;
    private String mode;
    private Jwt jwt;

    @Getter @Setter
    public static class Jwt {
        private String issuer;
        private String jwkSet;
        private String secret;
        private long expirationSeconds = 3600;
    }
}

