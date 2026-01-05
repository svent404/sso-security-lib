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
        private String secret;
        private long expirationSeconds = 3600;
        private Auth auth;
    }

    @Getter @Setter
    public static class Auth {
        private Converter converter;

        @Getter @Setter
        public static class Converter {
            private String resourceId;
            private String principleAttribute;
        }
    }
}

