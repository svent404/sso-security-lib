package it.svent404.security.model.response;

public record IntrospectionResponse(
        boolean active,
        String username,
        long exp
) {}

