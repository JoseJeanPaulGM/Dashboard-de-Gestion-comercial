package com.gestioncomercial.api.identity.application;

import com.gestioncomercial.api.shared.pagination.PageResult;
import com.gestioncomercial.api.shared.pagination.Pagination;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdentityService {
    private static final Pattern USERNAME = Pattern.compile("[a-z][a-z0-9._-]{2,63}");
    private static final Set<String> ROLES = Set.of("ADMIN", "VENDEDOR", "ALMACEN", "SUPERVISOR", "AUDITOR");
    private static final Duration ACCESS_LIFETIME = Duration.ofMinutes(15);
    private static final Duration REFRESH_LIFETIME = Duration.ofDays(7);
    private static final SecureRandom RANDOM = new SecureRandom();
    private final IdentityStore store;
    private final PasswordEncoder passwords;
    private final JwtEncoder jwt;
    private final Clock clock;

    public IdentityService(IdentityStore store, PasswordEncoder passwords, JwtEncoder jwt, Clock clock) {
        this.store = store;
        this.passwords = passwords;
        this.jwt = jwt;
        this.clock = clock;
    }

    @Transactional
    public IdentityUser create(String username, String password, Set<String> roles) {
        String normalized = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
        if (!USERNAME.matcher(normalized).matches()) {
            throw new IdentityException(400, "INVALID_USERNAME", "Invalid username", "Username must use 3-64 lowercase letters, digits, dots, underscores or hyphens.");
        }
        validatePassword(password);
        validateRoles(roles);
        if (store.findByUsername(normalized).isPresent()) {
            throw new IdentityException(409, "USERNAME_CONFLICT", "Username conflict", "Username is already in use.");
        }
        try {
            return store.create(normalized, passwords.encode(password), roles, Instant.now(clock));
        } catch (DataIntegrityViolationException exception) {
            throw new IdentityException(409, "USERNAME_CONFLICT", "Username conflict", "Username is already in use.");
        }
    }

    public IdentityUser get(long id) {
        return store.findById(id).orElseThrow(() ->
                new IdentityException(404, "USER_NOT_FOUND", "User not found", "User does not exist."));
    }

    public PageResult<IdentityUser> list(int page, int size, String sort) {
        return store.list(Pagination.parse(page, size, sort, Map.of("id", "id", "username", "username")));
    }

    @Transactional
    public IdentityUser replaceRoles(long id, Set<String> roles) {
        validateRoles(roles);
        store.lockAdministration();
        IdentityUser user = lockedUser(id);
        if (user.active() && user.roles().contains("ADMIN") && !roles.contains("ADMIN")
                && store.countActiveAdmins() <= 1) {
            throw new IdentityException(409, "LAST_ADMIN", "Last administrator", "The last active administrator cannot lose ADMIN.");
        }
        Instant now = Instant.now(clock);
        store.replaceRoles(id, roles, now);
        store.revokeAllSessions(id, now);
        return get(id);
    }

    @Transactional
    public IdentityUser setActive(long id, boolean active) {
        store.lockAdministration();
        IdentityUser user = lockedUser(id);
        if (!active && user.active() && user.roles().contains("ADMIN") && store.countActiveAdmins() <= 1) {
            throw new IdentityException(409, "LAST_ADMIN", "Last administrator", "The last active administrator cannot be deactivated.");
        }
        Instant now = Instant.now(clock);
        store.setActive(id, active, now);
        store.revokeAllSessions(id, now);
        return get(id);
    }

    @Transactional
    public void setPassword(long id, String password) {
        validatePassword(password);
        lockedUser(id);
        Instant now = Instant.now(clock);
        store.setPassword(id, passwords.encode(password), now);
        store.revokeAllSessions(id, now);
    }

    @Transactional
    public Tokens login(String username, String password) {
        if (password == null || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw invalidSession();
        }
        String normalized = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
        IdentityUser candidate = store.findByUsername(normalized).orElseThrow(this::invalidSession);
        IdentityUser user = store.lockUser(candidate.id()).orElseThrow(this::invalidSession);
        if (!user.active() || !passwords.matches(password, user.passwordHash())) {
            throw invalidSession();
        }
        return issue(user);
    }

    @Transactional
    public Tokens refresh(String refreshToken) {
        String tokenHash = hash(refreshToken);
        RefreshSession candidate = store.findSession(tokenHash).orElseThrow(this::invalidSession);
        IdentityUser user = store.lockUser(candidate.userId()).orElseThrow(this::invalidSession);
        RefreshSession session = store.lockSession(tokenHash).orElseThrow(this::invalidSession);
        Instant now = Instant.now(clock);
        if (session.revokedAt() != null || !session.expiresAt().isAfter(now)) {
            throw invalidSession();
        }
        if (!user.active()) {
            throw invalidSession();
        }
        store.revokeSession(session.id(), now);
        return issue(user);
    }

    @Transactional
    public void logout(String refreshToken) {
        store.lockSession(hash(refreshToken)).ifPresent(session ->
                store.revokeSession(session.id(), Instant.now(clock)));
    }

    private Tokens issue(IdentityUser user) {
        Instant now = Instant.now(clock);
        Instant accessExpiry = now.plus(ACCESS_LIFETIME);
        Instant refreshExpiry = now.plus(REFRESH_LIFETIME);
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer("gestion-comercial")
                .subject(Long.toString(user.id())).issuedAt(now).expiresAt(accessExpiry)
                .claim("version", user.tokenVersion()).build();
        String access = jwt.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String refresh = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        store.createSession(UUID.randomUUID(), user.id(), hash(refresh), now, refreshExpiry);
        return new Tokens(access, refresh, "Bearer", accessExpiry, refreshExpiry);
    }

    private String hash(String token) {
        if (token == null || token.isBlank()) {
            throw invalidSession();
        }
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private void validateRoles(Set<String> roles) {
        if (roles == null || roles.isEmpty() || roles.stream().anyMatch(java.util.Objects::isNull)
                || !ROLES.containsAll(roles)) {
            throw new IdentityException(400, "INVALID_ROLES", "Invalid roles", "At least one known role is required.");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 12
                || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new IdentityException(400, "INVALID_PASSWORD", "Invalid password", "Password must contain at least 12 characters and at most 72 UTF-8 bytes.");
        }
    }

    private IdentityUser lockedUser(long id) {
        return store.lockUser(id).orElseThrow(() ->
                new IdentityException(404, "USER_NOT_FOUND", "User not found", "User does not exist."));
    }

    private IdentityException invalidSession() {
        return new IdentityException(401, "INVALID_CREDENTIALS", "Authentication failed", "Credentials or session are invalid.");
    }

    public record Tokens(String accessToken, String refreshToken, String tokenType,
                         Instant accessExpiresAt, Instant refreshExpiresAt) {
    }
}
