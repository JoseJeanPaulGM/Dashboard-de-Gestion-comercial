package com.gestioncomercial.api.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestioncomercial.api.identity.application.IdentityService;
import com.gestioncomercial.api.identity.application.IdentityStore;
import com.gestioncomercial.api.identity.application.IdentityUser;
import com.gestioncomercial.api.support.postgresql.PostgreSqlTestConfiguration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Future;
import com.gestioncomercial.api.identity.application.IdentityException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc(print = org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint.NONE)
@ActiveProfiles("test")
@Import(PostgreSqlTestConfiguration.class)
class IdentityApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired IdentityService identity;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper json;
    @Autowired JwtEncoder jwt;
    @Autowired JwtDecoder decoder;
    @Autowired IdentityStore store;

    @BeforeEach
    void cleanIdentity() {
        jdbc.execute("TRUNCATE TABLE refresh_sessions, user_roles, app_users RESTART IDENTITY");
    }

    @Test
    void enforcesAuthenticationAndRolePermissionsUsingRealTokens() throws Exception {
        mvc.perform(get("/api/v1/customers"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));

        String vendorPassword = randomPassword();
        identity.create("vendor", vendorPassword, Set.of("VENDEDOR"));
        String vendor = login("vendor", vendorPassword).get("accessToken").asText();
        var decoded = decoder.decode(vendor);
        assertThat(decoded.getSubject()).isEqualTo("1");
        assertThat(decoded.getClaimAsString("iss")).isEqualTo("gestion-comercial");
        assertThat(((Number) decoded.getClaim("version")).intValue()).isEqualTo(0);
        assertThat(store.findById(1).orElseThrow().permissions()).contains("CUSTOMER_READ");
        mvc.perform(get("/api/v1/customers").header("Authorization", bearer(vendor)))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/customers/999999").header("Authorization", bearer(vendor)))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/v1/inventory/adjustments").header("Authorization", bearer(vendor))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
        mvc.perform(get("/api/v1/users").header("Authorization", bearer(vendor)))
                .andExpect(status().isForbidden());

        String warehousePassword = randomPassword();
        identity.create("warehouse", warehousePassword, Set.of("ALMACEN"));
        String warehouse = login("warehouse", warehousePassword).get("accessToken").asText();
        mvc.perform(get("/api/v1/inventory/alerts").header("Authorization", bearer(warehouse)))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/customers").header("Authorization", bearer(warehouse)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/sales").header("Authorization", bearer(warehouse)))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/v1/customers").header("Authorization", "Bearer broken"))
                .andExpect(status().isUnauthorized());
        String missingIssuer = jwt.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),
                JwtClaimsSet.builder().subject("1").issuedAt(Instant.now())
                        .expiresAt(Instant.now().plusSeconds(300)).claim("version", 0).build())).getTokenValue();
        mvc.perform(get("/api/v1/customers").header("Authorization", bearer(missingIssuer)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rotatesRefreshTokensRejectsReplayAndRevokesOnLogoutOrDeactivation() throws Exception {
        String password = randomPassword();
        IdentityUser user = identity.create("sessionuser", password, Set.of("VENDEDOR"));
        JsonNode first = login("sessionuser", password);
        String firstRefresh = first.get("refreshToken").asText();
        assertThat(jdbc.queryForObject("SELECT token_hash FROM refresh_sessions WHERE user_id = ?",
                String.class, user.id())).hasSize(64).isNotEqualTo(firstRefresh);

        JsonNode second = json.readTree(mvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("refreshToken", firstRefresh))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        mvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("refreshToken", firstRefresh))))
                .andExpect(status().isUnauthorized());

        String secondRefresh = second.get("refreshToken").asText();
        mvc.perform(post("/api/v1/auth/logout").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("refreshToken", secondRefresh))))
                .andExpect(status().isNoContent());
        mvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("refreshToken", secondRefresh))))
                .andExpect(status().isUnauthorized());

        String currentAccess = login("sessionuser", password).get("accessToken").asText();
        identity.setActive(user.id(), false);
        mvc.perform(get("/api/v1/customers").header("Authorization", bearer(currentAccess)))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("username", "sessionuser", "password", password))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminManagesUsersAndPrivilegeChangesInvalidateTokens() throws Exception {
        String adminPassword = randomPassword();
        IdentityUser admin = identity.create("admin", adminPassword, Set.of("ADMIN"));
        String adminToken = login("admin", adminPassword).get("accessToken").asText();
        String managedPassword = randomPassword();
        JsonNode created = json.readTree(mvc.perform(post("/api/v1/users")
                        .header("Authorization", bearer(adminToken)).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("username", "managed", "password", managedPassword,
                                "roles", Set.of("VENDEDOR")))))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.username").value("managed"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andReturn().getResponse().getContentAsString());
        long managedId = created.get("id").asLong();
        String oldAccess = login("managed", managedPassword).get("accessToken").asText();
        mvc.perform(put("/api/v1/users/{id}/roles", managedId)
                        .header("Authorization", bearer(adminToken)).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("roles", Set.of("ALMACEN")))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.roles[0]").value("ALMACEN"));
        mvc.perform(get("/api/v1/customers").header("Authorization", bearer(oldAccess)))
                .andExpect(status().isUnauthorized());
        String newAccess = login("managed", managedPassword).get("accessToken").asText();
        mvc.perform(get("/api/v1/inventory/alerts").header("Authorization", bearer(newAccess)))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/users?page=0&size=10&sort=username,asc")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(2));

        String replacement = randomPassword();
        mvc.perform(put("/api/v1/users/{id}/password", managedId)
                        .header("Authorization", bearer(adminToken)).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("password", replacement))))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/inventory/alerts").header("Authorization", bearer(newAccess)))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("username", "managed", "password", managedPassword))))
                .andExpect(status().isUnauthorized());
        assertThat(login("managed", replacement).get("accessToken").asText()).isNotBlank();

        mvc.perform(patch("/api/v1/users/{id}/status", admin.id())
                        .header("Authorization", bearer(adminToken)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":false}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("LAST_ADMIN"));
    }

    private JsonNode login(String username, String password) throws Exception {
        return json.readTree(mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("username", username, "password", password))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    }

    @Test
    void rejectsIncompleteExpiredOrWronglyTypedJwtClaims() throws Exception {
        String password = randomPassword();
        IdentityUser user = identity.create("jwtclaims", password, Set.of("VENDEDOR"));
        Instant now = Instant.now();
        List<JwtClaimsSet> invalidClaims = List.of(
                JwtClaimsSet.builder().issuer("gestion-comercial").subject(Long.toString(user.id()))
                        .issuedAt(now).claim("version", 0).build(),
                JwtClaimsSet.builder().issuer("gestion-comercial").subject(Long.toString(user.id()))
                        .issuedAt(now.minusSeconds(120)).expiresAt(now.minusSeconds(1)).claim("version", 0).build(),
                JwtClaimsSet.builder().issuer("gestion-comercial").subject(Long.toString(user.id()))
                        .issuedAt(now).expiresAt(now.plusSeconds(300)).claim("version", "0").build(),
                JwtClaimsSet.builder().issuer("other-service").subject(Long.toString(user.id()))
                        .issuedAt(now).expiresAt(now.plusSeconds(300)).claim("version", 0).build(),
                JwtClaimsSet.builder().issuer("gestion-comercial").subject("invalid")
                        .issuedAt(now).expiresAt(now.plusSeconds(300)).claim("version", 0).build());
        for (JwtClaimsSet claims : invalidClaims) {
            String token = jwt.encode(JwtEncoderParameters.from(
                    JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
            mvc.perform(get("/api/v1/customers").header("Authorization", bearer(token)))
                    .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
        }
    }

    @Test
    void validatesPasswordsAndRejectsExpiredRefreshSessions() throws Exception {
        String password = randomPassword();
        IdentityUser admin = identity.create("admin", password, Set.of("ADMIN"));
        String adminToken = login("admin", password).get("accessToken").asText();
        for (String invalid : List.of("a".repeat(73), "á".repeat(37), "short")) {
            mvc.perform(post("/api/v1/users").header("Authorization", bearer(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json.writeValueAsString(Map.of("username", "invalidpwd", "password", invalid,
                                    "roles", Set.of("VENDEDOR")))))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_PASSWORD"));
        }
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("username", "admin", "password", "a".repeat(73)))))
                .andExpect(status().isUnauthorized());
        String refresh = login("admin", password).get("refreshToken").asText();
        jdbc.update("UPDATE refresh_sessions SET created_at = CURRENT_TIMESTAMP - INTERVAL '2 days', "
                + "expires_at = CURRENT_TIMESTAMP - INTERVAL '1 day' WHERE user_id = ?", admin.id());
        mvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("refreshToken", refresh))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void concurrentRefreshRotatesOnlyOnce() throws Exception {
        String password = randomPassword();
        identity.create("concurrentrefresh", password, Set.of("VENDEDOR"));
        String refresh = login("concurrentrefresh", password).get("refreshToken").asText();
        var executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            java.util.concurrent.Callable<Integer> attempt = () -> {
                start.await(5, TimeUnit.SECONDS);
                try { identity.refresh(refresh); return 200; }
                catch (IdentityException exception) { return exception.status(); }
            };
            Future<Integer> first = executor.submit(attempt);
            Future<Integer> second = executor.submit(attempt);
            start.countDown();
            assertThat(List.of(first.get(10, TimeUnit.SECONDS), second.get(10, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(200, 401);
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM refresh_sessions WHERE revoked_at IS NULL", Long.class))
                    .isEqualTo(1);
        } finally { executor.shutdownNow(); }
    }

    @Test
    void concurrentAdminDeactivationPreservesOneActiveAdministrator() throws Exception {
        IdentityUser firstAdmin = identity.create("firstadmin", randomPassword(), Set.of("ADMIN"));
        IdentityUser secondAdmin = identity.create("secondadmin", randomPassword(), Set.of("ADMIN"));
        var executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<Integer> first = executor.submit(() -> deactivateAfter(start, firstAdmin.id()));
            Future<Integer> second = executor.submit(() -> deactivateAfter(start, secondAdmin.id()));
            start.countDown();
            assertThat(List.of(first.get(10, TimeUnit.SECONDS), second.get(10, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(200, 409);
            assertThat(store.countActiveAdmins()).isEqualTo(1);
        } finally { executor.shutdownNow(); }
    }

    @Test
    void concurrentRefreshAndDeactivationLeaveNoActiveSession() throws Exception {
        String password = randomPassword();
        IdentityUser user = identity.create("sessionrace", password, Set.of("VENDEDOR"));
        String refresh = login("sessionrace", password).get("refreshToken").asText();
        var executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<Integer> renewal = executor.submit(() -> {
                start.await(5, TimeUnit.SECONDS);
                try { identity.refresh(refresh); return 200; }
                catch (IdentityException exception) { return exception.status(); }
            });
            Future<Integer> deactivation = executor.submit(() -> deactivateAfter(start, user.id()));
            start.countDown();
            assertThat(renewal.get(10, TimeUnit.SECONDS)).isIn(200, 401);
            assertThat(deactivation.get(10, TimeUnit.SECONDS)).isEqualTo(200);
            assertThat(store.findById(user.id()).orElseThrow().active()).isFalse();
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM refresh_sessions WHERE user_id = ? AND revoked_at IS NULL",
                    Long.class, user.id())).isZero();
        } finally { executor.shutdownNow(); }
    }

    private int deactivateAfter(CountDownLatch start, long userId) throws InterruptedException {
        start.await(5, TimeUnit.SECONDS);
        try { identity.setActive(userId, false); return 200; }
        catch (IdentityException exception) { return exception.status(); }
    }

    @Test
    void appliesTheCompleteRoleMatrixToExistingBusinessEndpoints() throws Exception {
        String[] roles = {"ADMIN", "VENDEDOR", "ALMACEN", "SUPERVISOR", "AUDITOR"};
        String[] paths = {"customers", "categories", "products", "suppliers", "purchases", "sales", "inventory/alerts"};
        boolean[][] reads = {
                {true, true, true, true, true, true, true},
                {true, true, true, true, false, true, false},
                {false, true, true, true, true, false, true},
                {true, true, true, true, true, true, true},
                {true, true, true, true, true, true, true}};
        boolean[][] writes = {
                {true, true, true, true, true, true, true},
                {true, false, false, false, false, true, false},
                {false, true, true, true, true, false, true},
                {false, false, false, false, false, false, false},
                {false, false, false, false, false, false, false}};
        for (int roleIndex = 0; roleIndex < roles.length; roleIndex++) {
            String username = "role_" + roles[roleIndex].toLowerCase(java.util.Locale.ROOT);
            String password = randomPassword();
            identity.create(username, password, Set.of(roles[roleIndex]));
            String token = login(username, password).get("accessToken").asText();
            for (int pathIndex = 0; pathIndex < paths.length; pathIndex++) {
                mvc.perform(get("/api/v1/" + paths[pathIndex]).header("Authorization", bearer(token)))
                        .andExpect(status().is(reads[roleIndex][pathIndex] ? 200 : 403));
                String writePath = pathIndex == 6 ? "inventory/adjustments" : paths[pathIndex];
                mvc.perform(post("/api/v1/" + writePath).header("Authorization", bearer(token))
                                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                        .andExpect(status().is(writes[roleIndex][pathIndex] ? 400 : 403));
            }
        }
    }

    @Test
    void rejectsInvalidUserDataAndSupportsLargeValidPageNumbers() throws Exception {
        String password = randomPassword();
        IdentityUser admin = identity.create("admin", password, Set.of("ADMIN"));
        String token = login("admin", password).get("accessToken").asText();
        mvc.perform(post("/api/v1/users").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("username", "ADMIN", "password", randomPassword(), "roles", Set.of("ADMIN")))))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("USERNAME_CONFLICT"));
        mvc.perform(post("/api/v1/users").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("username", "invalid-role", "password", randomPassword(), "roles", Set.of("UNKNOWN")))))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_ROLES"));
        mvc.perform(post("/api/v1/users").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("username", "nullrole", "password", randomPassword()))
                                .replace("}", ",\"roles\":[null]}")))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/users?page=2147483647&size=100").header("Authorization", bearer(token)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content").isEmpty());
        mvc.perform(put("/api/v1/users/{id}/roles", admin.id()).header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"roles\":[\"VENDEDOR\"]}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("LAST_ADMIN"));
        mvc.perform(get("/api/v1/unknown").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    private String randomPassword() {
        return "test-" + UUID.randomUUID() + "-local";
    }

    private String bearer(String token) { return "Bearer " + token; }
}
