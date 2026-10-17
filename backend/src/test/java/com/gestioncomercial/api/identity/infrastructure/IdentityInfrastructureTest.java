package com.gestioncomercial.api.identity.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.gestioncomercial.api.identity.application.IdentityService;
import com.gestioncomercial.api.identity.application.IdentityStore;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

class IdentityInfrastructureTest {
    @Test
    void rejectsMissingOrShortSigningKeys() {
        JwtConfiguration config = new JwtConfiguration();
        for (String key : new String[] {"", "invalid-base64", Base64.getEncoder().encodeToString(new byte[8])}) {
            assertThatThrownBy(() -> config.jwtSecret(key)).isInstanceOf(IllegalStateException.class);
        }
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        assertThat(config.jwtSecret(Base64.getEncoder().encodeToString(bytes)).getEncoded()).isEqualTo(bytes);
    }

    @Test
    void bootstrapsOnlyAnEmptyDatabaseWithExplicitCredentials() throws Exception {
        IdentityStore store = mock(IdentityStore.class);
        IdentityService service = mock(IdentityService.class);
        AdminBootstrap bootstrap = new AdminBootstrap();
        var args = new DefaultApplicationArguments(new String[0]);
        String password = "test-" + UUID.randomUUID();
        bootstrap.bootstrapAdmin(store, service, false, "", "").run(args);
        verifyNoInteractions(service);
        when(store.countUsers()).thenReturn(1L);
        bootstrap.bootstrapAdmin(store, service, true, "", "").run(args);
        verifyNoInteractions(service);
        when(store.countUsers()).thenReturn(0L);
        assertThatThrownBy(() -> bootstrap.bootstrapAdmin(store, service, true, "", "").run(args))
                .isInstanceOf(IllegalStateException.class);
        bootstrap.bootstrapAdmin(store, service, true, "admin", password).run(args);
        verify(service).create("admin", password, Set.of("ADMIN"));
    }
}
