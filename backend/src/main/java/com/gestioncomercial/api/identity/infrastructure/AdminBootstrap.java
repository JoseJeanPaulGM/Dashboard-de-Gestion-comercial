package com.gestioncomercial.api.identity.infrastructure;

import com.gestioncomercial.api.identity.application.IdentityService;
import com.gestioncomercial.api.identity.application.IdentityStore;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminBootstrap {
    @Bean
    ApplicationRunner bootstrapAdmin(IdentityStore store, IdentityService service,
                                     @Value("${identity.bootstrap.enabled:true}") boolean enabled,
                                     @Value("${identity.bootstrap.username:}") String username,
                                     @Value("${identity.bootstrap.password:}") String password) {
        return args -> {
            if (!enabled || store.countUsers() > 0) {
                return;
            }
            if (username.isBlank() || password.isBlank()) {
                throw new IllegalStateException("Configure BOOTSTRAP_ADMIN_USERNAME and BOOTSTRAP_ADMIN_PASSWORD for the first startup");
            }
            service.create(username, password, Set.of("ADMIN"));
        };
    }
}
