package it.svent404.security.repository;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryTokenRepository {
    private final Map<String, Instant> revoked = new ConcurrentHashMap<>();

    public void store(String token, Instant exp) {}

    public boolean isRevoked(String token) {
        return revoked.containsKey(token);
    }

    public void revoke(String token) {
        revoked.put(token, Instant.now());
    }
}
