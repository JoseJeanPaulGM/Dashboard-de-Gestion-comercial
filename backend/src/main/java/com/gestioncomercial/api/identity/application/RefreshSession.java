package com.gestioncomercial.api.identity.application;

import java.time.Instant;
import java.util.UUID;

public record RefreshSession(UUID id, long userId, Instant expiresAt, Instant revokedAt) {
}
