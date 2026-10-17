package com.gestioncomercial.api.identity.infrastructure;

import com.gestioncomercial.api.identity.application.IdentityStore;
import com.gestioncomercial.api.identity.application.IdentityUser;
import com.gestioncomercial.api.identity.application.RefreshSession;
import com.gestioncomercial.api.shared.pagination.PageCriteria;
import com.gestioncomercial.api.shared.pagination.PageResult;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcIdentityStore implements IdentityStore {
    private final JdbcTemplate jdbc;

    public JdbcIdentityStore(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public long countUsers() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM app_users", Long.class);
    }

    @Override
    public long countActiveAdmins() {
        return jdbc.queryForObject("""
                SELECT COUNT(*) FROM app_users u JOIN user_roles r ON r.user_id = u.id
                WHERE u.active = TRUE AND r.role_code = 'ADMIN'
                """, Long.class);
    }

    @Override
    public Optional<IdentityUser> findById(long id) {
        return jdbc.query("SELECT * FROM app_users WHERE id = ?", this::mapUser, id).stream().findFirst();
    }

    @Override
    public Optional<IdentityUser> lockUser(long id) {
        return jdbc.query("SELECT * FROM app_users WHERE id = ? FOR UPDATE", this::mapUser, id).stream().findFirst();
    }

    @Override
    public void lockAdministration() {
        jdbc.execute("SELECT pg_advisory_xact_lock(10100001)");
    }

    @Override
    public Optional<IdentityUser> findByUsername(String username) {
        return jdbc.query("SELECT * FROM app_users WHERE username = ?", this::mapUser, username).stream().findFirst();
    }

    @Override
    public PageResult<IdentityUser> list(PageCriteria page) {
        long total = countUsers();
        long offset = Math.multiplyExact((long) page.page(), page.size());
        String sql = "SELECT * FROM app_users ORDER BY " + page.sortProperty() + " " + page.direction().name()
                + " LIMIT ? OFFSET ?";
        List<IdentityUser> content = jdbc.query(sql, this::mapUser, page.size(), offset);
        return new PageResult<>(content, page.page(), page.size(), total,
                Math.toIntExact((total + page.size() - 1) / page.size()));
    }

    @Override
    public IdentityUser create(String username, String passwordHash, Set<String> roles, Instant now) {
        Long id = jdbc.queryForObject("""
                INSERT INTO app_users(username, password_hash, created_at, updated_at)
                VALUES (?, ?, ?, ?) RETURNING id
                """, Long.class, username, passwordHash, Timestamp.from(now), Timestamp.from(now));
        insertRoles(id, roles);
        return findById(id).orElseThrow();
    }

    @Override
    public void replaceRoles(long id, Set<String> roles, Instant now) {
        jdbc.update("DELETE FROM user_roles WHERE user_id = ?", id);
        insertRoles(id, roles);
        jdbc.update("UPDATE app_users SET token_version = token_version + 1, updated_at = ? WHERE id = ?",
                Timestamp.from(now), id);
    }

    @Override
    public void setActive(long id, boolean active, Instant now) {
        jdbc.update("UPDATE app_users SET active = ?, token_version = token_version + 1, updated_at = ? WHERE id = ?",
                active, Timestamp.from(now), id);
    }

    @Override
    public void setPassword(long id, String passwordHash, Instant now) {
        jdbc.update("UPDATE app_users SET password_hash = ?, token_version = token_version + 1, updated_at = ? WHERE id = ?",
                passwordHash, Timestamp.from(now), id);
    }

    @Override
    public void createSession(UUID id, long userId, String tokenHash, Instant createdAt, Instant expiresAt) {
        jdbc.update("""
                INSERT INTO refresh_sessions(id, user_id, token_hash, created_at, expires_at)
                VALUES (?, ?, ?, ?, ?)
                """, id, userId, tokenHash, Timestamp.from(createdAt), Timestamp.from(expiresAt));
    }

    @Override
    public Optional<RefreshSession> lockSession(String tokenHash) {
        return jdbc.query("SELECT id, user_id, expires_at, revoked_at FROM refresh_sessions WHERE token_hash = ? FOR UPDATE",
                (rs, row) -> new RefreshSession(rs.getObject("id", UUID.class), rs.getLong("user_id"),
                        rs.getTimestamp("expires_at").toInstant(),
                        rs.getTimestamp("revoked_at") == null ? null : rs.getTimestamp("revoked_at").toInstant()),
                tokenHash).stream().findFirst();
    }

    @Override
    public Optional<RefreshSession> findSession(String tokenHash) {
        return jdbc.query("SELECT id, user_id, expires_at, revoked_at FROM refresh_sessions WHERE token_hash = ?",
                (rs, row) -> new RefreshSession(rs.getObject("id", UUID.class), rs.getLong("user_id"),
                        rs.getTimestamp("expires_at").toInstant(),
                        rs.getTimestamp("revoked_at") == null ? null : rs.getTimestamp("revoked_at").toInstant()),
                tokenHash).stream().findFirst();
    }

    @Override
    public void revokeSession(UUID id, Instant now) {
        jdbc.update("UPDATE refresh_sessions SET revoked_at = ? WHERE id = ? AND revoked_at IS NULL",
                Timestamp.from(now), id);
    }

    @Override
    public void revokeAllSessions(long userId, Instant now) {
        jdbc.update("UPDATE refresh_sessions SET revoked_at = ? WHERE user_id = ? AND revoked_at IS NULL",
                Timestamp.from(now), userId);
    }

    private IdentityUser mapUser(ResultSet rs, int row) throws SQLException {
        long id = rs.getLong("id");
        Set<String> roles = new HashSet<>(jdbc.queryForList(
                "SELECT role_code FROM user_roles WHERE user_id = ?", String.class, id));
        Set<String> permissions = new HashSet<>(jdbc.queryForList("""
                SELECT DISTINCT rp.permission_code FROM user_roles ur
                JOIN role_permissions rp ON rp.role_code = ur.role_code
                WHERE ur.user_id = ?
                """, String.class, id));
        return new IdentityUser(id, rs.getString("username"), rs.getString("password_hash"),
                rs.getBoolean("active"), rs.getInt("token_version"), Set.copyOf(roles), Set.copyOf(permissions));
    }

    private void insertRoles(long id, Set<String> roles) {
        for (String role : roles) {
            jdbc.update("INSERT INTO user_roles(user_id, role_code) VALUES (?, ?)", id, role);
        }
    }
}
