package com.gestioncomercial.api.identity.api;

import com.gestioncomercial.api.identity.application.IdentityService;
import com.gestioncomercial.api.identity.application.IdentityUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final IdentityService service;

    public AuthController(IdentityService service) { this.service = service; }

    @PostMapping("/login")
    @SecurityRequirements
    IdentityService.Tokens login(@Valid @RequestBody LoginRequest request) {
        return service.login(request.username(), request.password());
    }

    @PostMapping("/refresh")
    @SecurityRequirements
    IdentityService.Tokens refresh(@Valid @RequestBody RefreshRequest request) {
        return service.refresh(request.refreshToken());
    }

    @PostMapping("/logout")
    @SecurityRequirements
    ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
        service.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    UserController.UserResponse me(Authentication authentication) {
        IdentityUser user = service.get(Long.parseLong(authentication.getName()));
        return UserController.UserResponse.from(user);
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) { }
    public record RefreshRequest(@NotBlank String refreshToken) { }
}
