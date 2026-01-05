package it.svent404.security.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import it.svent404.security.model.response.IntrospectionResponse;
import it.svent404.security.model.response.TokenResponse;
import it.svent404.security.repository.InMemoryTokenRepository;
import it.svent404.security.properties.SsoSecurityProperties;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.List;

public class LocalJwtService {

    private final SsoSecurityProperties props;
    private final SecretKey key;
    private final Clock clock;
    private final InMemoryTokenRepository tokenRepository;

    public LocalJwtService(
            SsoSecurityProperties props,
            Clock clock,
            InMemoryTokenRepository tokenRepository
    ) {
        this.props = props;
        this.clock = clock;
        this.tokenRepository = tokenRepository;
        this.key = Keys.hmacShaKeyFor(props.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public TokenResponse generate(Authentication auth) {

        Instant now = Instant.now(clock);
        Instant exp = now.plusSeconds(props.getJwt().getExpirationSeconds());

        String token = Jwts.builder()
                .subject(auth.getName())
                .issuer(props.getJwt().getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claim("roles", auth.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList()
                )
                .signWith(key)
                .compact();

        return new TokenResponse(token, null, "Bearer", props.getJwt().getExpirationSeconds());
    }

    public TokenResponse refresh(String refreshToken) {
        if (!validate(refreshToken)) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        Claims claims = parse(refreshToken);
        Authentication auth = new UsernamePasswordAuthenticationToken(
                claims.getSubject(), null,
                extractAuthorities(claims)
        );

        return generate(auth);
    }

    public boolean validate(String token) {
        try {
            parse(token);
            return !tokenRepository.isRevoked(token);
        } catch (JwtException ex) {
            return false;
        }
    }

    public IntrospectionResponse introspect(String token) {
        try {
            Claims claims = parse(token);
            return new IntrospectionResponse(
                    true,
                    claims.getSubject(),
                    claims.getExpiration().toInstant().getEpochSecond()
            );
        } catch (JwtException ex) {
            return new IntrospectionResponse(false, null, 0);
        }
    }

    public void invalidate(String token) {
        tokenRepository.revoke(token);
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private List<SimpleGrantedAuthority> extractAuthorities(Claims claims) {
        List<String> roles = claims.get("roles", List.class);
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    public Authentication toAuthentication(String token) {

        if (!validate(token)) {
            throw new BadCredentialsException("Invalid JWT token");
        }

        Claims claims = parse(token);

        String username = claims.getSubject();

        List<SimpleGrantedAuthority> authorities = extractAuthorities(claims);

        return new UsernamePasswordAuthenticationToken(
                username,
                token,
                authorities
        );
    }
}

