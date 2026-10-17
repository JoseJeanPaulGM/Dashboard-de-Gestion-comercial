package com.gestioncomercial.api.identity.api;

import com.gestioncomercial.api.identity.application.IdentityService;
import com.gestioncomercial.api.identity.application.IdentityUser;
import com.gestioncomercial.api.shared.api.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.util.Set;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final IdentityService service;

    public UserController(IdentityService service) { this.service = service; }

    @PostMapping
    ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        IdentityUser user = service.create(request.username(), request.password(), request.roles());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(user.id()).toUri();
        return ResponseEntity.created(location).body(UserResponse.from(user));
    }

    @GetMapping("/{id}")
    UserResponse get(@PathVariable long id) { return UserResponse.from(service.get(id)); }

    @GetMapping
    PageResponse<UserResponse> list(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size,
                                    @RequestParam(defaultValue = "id,asc") String sort) {
        return PageResponse.from(service.list(page, size, sort), UserResponse::from);
    }

    @PutMapping("/{id}/roles")
    UserResponse roles(@PathVariable long id, @Valid @RequestBody RolesRequest request) {
        return UserResponse.from(service.replaceRoles(id, request.roles()));
    }

    @PatchMapping("/{id}/status")
    UserResponse status(@PathVariable long id, @Valid @RequestBody StatusRequest request) {
        return UserResponse.from(service.setActive(id, request.active()));
    }

    @PutMapping("/{id}/password")
    ResponseEntity<Void> password(@PathVariable long id, @Valid @RequestBody PasswordRequest request) {
        service.setPassword(id, request.password());
        return ResponseEntity.noContent().build();
    }

    public record CreateUserRequest(@NotBlank String username, @NotBlank String password,
                                    @NotEmpty Set<String> roles) { }
    public record RolesRequest(@NotEmpty Set<String> roles) { }
    public record StatusRequest(@NotNull Boolean active) { }
    public record PasswordRequest(@NotBlank String password) { }
    public record UserResponse(long id, String username, boolean active, Set<String> roles) {
        public static UserResponse from(IdentityUser user) {
            return new UserResponse(user.id(), user.username(), user.active(), user.roles());
        }
    }
}
