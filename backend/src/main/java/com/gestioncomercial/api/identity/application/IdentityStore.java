package com.gestioncomercial.api.identity.application;

import com.gestioncomercial.api.shared.pagination.PageCriteria;
import com.gestioncomercial.api.shared.pagination.PageResult;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface IdentityStore {
    long countUsers();
    long countActiveAdmins();
    Optional<IdentityUser> findById(long id);
    Optional<IdentityUser> lockUser(long id);
    void lockAdministration();
    Optional<IdentityUser> findByUsername(String username);
    PageResult<IdentityUser> list(PageCriteria page);
    IdentityUser create(String username, String passwordHash, Set<String> roles, Instant now);
    void replaceRoles(long id, Set<String> roles, Instant now);
    void setActive(long id, boolean active, Instant now);
    void setPassword(long id, String passwordHash, Instant now);
    void createSession(UUID id, long userId, String tokenHash, Instant createdAt, Instant expiresAt);
    Optional<RefreshSession> lockSession(String tokenHash);
    Optional<RefreshSession> findSession(String tokenHash);
    void revokeSession(UUID id, Instant now);
    void revokeAllSessions(long userId, Instant now);
}
