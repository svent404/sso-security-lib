package it.svent404.security.model.request;

public record LoginRequest(
        String username,
        String password
) {}