# 🛡️ sso-security-lib

A simple custom **Spring Boot Security Starter** that provides a **plug-and-play SSO module** based on:

- 🔐 **Local JWT (dev / test)**
- 🔑 **Keycloak (prod)**
- 🍃 **Spring Boot auto-configuration**

The goal of this library is to allow any Spring Boot application to **enable SSO security without writing any code**, 
simply by choosing the desired mode and setting the required configuration parameters.

---

## ✨ Features

- Custom `SecurityFilterChain`
- Automatic JWT validation and authorization
- Role / authority support
- `/auth/**` endpoints to test the library (local mode only)
- Optional Swagger / OpenAPI integration
- No component scan required
- No manual Spring Security configuration

---

## 📦 Installation

### Maven

```xml
<dependency>
  <groupId>it.svent404.security</groupId>
  <artifactId>sso-security-lib</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```

---

## ⚙️ Operating Modes

The library supports **two mutually exclusive modes**, selectable via configuration.

---

## 🟢 Mode 1 — Local JWT (DEV / TEST)

This mode is intended for:
- local development
- automated tests
- environments without an external Identity Provider

### 🔧 Configuration

```yaml
sso:
  enabled: true
  mode: local
  jwt:
    secret: change-me
    expiration-seconds: 3600
```

### 🔐 What it provides

- Authentication endpoints:
  - `POST /auth/token`
  - `POST /auth/refresh`
  - `POST /auth/introspect`
  - `GET  /auth/userinfo`
  - `POST /auth/logout`
- Locally signed JWTs
- Automatic `Authentication` creation
- `SecurityContextHolder` fully managed by the library

### 🧪 Default users

For local testing, an in-memory `UserDetailsService` is registered (if none is provided):

```text
username: admin
password: admin
roles: ADMIN

username: user
password: user
roles: USER
```


---

## 🔵 Mode 2 — Keycloak / OAuth2 (PROD)

This mode is intended for:
- production environments
- Keycloak integration
- any OAuth2 / OIDC compatible provider

### 🔧 Configuration

```yaml
sso:
  enabled: true
  mode: oauth2
  jwt:
    auth:
      converter:
        resource-id: sso-security-lib
        principle-attribute: preferred_username

spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:9090/realms/{change me with the realm name defined in keycloak}
          jwk-set-uri: http://localhost:9090/realms/{change me with the realm name defined in keycloak}/protocol/openid-connect/certs

```

Here, a `docker-compose.yaml` file is also provided to start Keycloak on Podman Desktop, or a similar containerization environment:

```yaml
services:
  keycloak:
    container_name: keycloak_demo
    image: quay.io/keycloak/keycloak:25.0.5
    ports:
      - 9090:8080
    environment:
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin
    volumes:
      - keycloak_data:/opt/keycloak/data
    command:
      - 'start-dev'

volumes:
  keycloak_data:
```

### 🔐 What the library does

- Automatically configures the application as an **OAuth2 Resource Server**
- Validates JWTs issued by Keycloak
- Maps claims to `GrantedAuthority`
- Protects all endpoints that are not explicitly marked as `permitAll()`

📌 **No `/auth/**` endpoints are exposed in this mode**  
📌 Authentication is fully delegated to Keycloak


---

## 🔒 Security Behavior

For both modes:

- **STATELESS** session management
- All HTTP requests pass through the library security filters
- Non-public endpoints require a valid **Bearer Token**
- Missing or invalid tokens result in **401 / 403** responses

---

## 📖 Swagger / OpenAPI (Optional)

The library **does not force Swagger**, but provides:
- Bearer JWT `SecurityScheme`
- Support for `@SecurityRequirement`

### Enable Swagger in your application

In the consumer project:

```xml
<dependency>
  <groupId>org.springdoc</groupId>
  <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
</dependency>
```

In the application.yaml:

```yaml
springdoc:
  api-docs:
    enabled: true
  swagger-ui:
    enabled: true
```

Available endpoints:
- `/swagger-ui.html`
- `/v3/api-docs`

---

## 🛠️ Requirements

- Java 21+
- Spring Boot 4.0.x

---

## 📄 License

MIT

