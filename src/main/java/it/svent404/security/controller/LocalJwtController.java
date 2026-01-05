package it.svent404.security.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import it.svent404.security.model.request.LoginRequest;
import it.svent404.security.model.response.IntrospectionResponse;
import it.svent404.security.model.response.TokenResponse;
import it.svent404.security.model.response.UserInfoResponse;
import it.svent404.security.service.LocalJwtService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class LocalJwtController {

    private final AuthenticationManager authenticationManager;
    private final LocalJwtService jwtService;

    @PostMapping("/token")
    public TokenResponse token(@RequestBody LoginRequest request) {

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(), request.password()
                )
        );

        return jwtService.generate(auth);
    }

    @PostMapping("/refresh")
    @SecurityRequirement(name = "bearerAuth")
    public TokenResponse refresh(@RequestBody String refreshToken) {
        return jwtService.refresh(refreshToken);
    }

    @GetMapping("/userinfo")
    @SecurityRequirement(name = "bearerAuth")
    public UserInfoResponse userinfo(Authentication authentication) {
        return UserInfoResponse.from(authentication);
    }

    @PostMapping("/introspect")
    @SecurityRequirement(name = "bearerAuth")
    public IntrospectionResponse introspect(@RequestBody String token) {
        return jwtService.introspect(token);
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "bearerAuth")
    public void logout(@RequestBody String token) {
        jwtService.invalidate(token);
    }
}

