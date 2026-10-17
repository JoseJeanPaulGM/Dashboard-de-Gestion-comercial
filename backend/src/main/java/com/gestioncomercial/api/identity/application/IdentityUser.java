package com.gestioncomercial.api.identity.application;

import java.util.Set;

public record IdentityUser(long id, String username, String passwordHash, boolean active,
                           int tokenVersion, Set<String> roles, Set<String> permissions) {
}
