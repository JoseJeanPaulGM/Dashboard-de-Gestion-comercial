package com.gestioncomercial.api.identity.infrastructure;

import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

@Configuration
public class JwtConfiguration {
    @Bean
    @Profile("!test")
    SecretKey jwtSecret(@Value("${identity.jwt.secret:}") String encoded) {
        byte[] key;
        try {
            key = Base64.getDecoder().decode(encoded);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("JWT_SECRET must be a Base64-encoded 32-byte key", exception);
        }
        if (key.length < 32) {
            throw new IllegalStateException("JWT_SECRET must be a Base64-encoded 32-byte key");
        }
        return new SecretKeySpec(key, "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey secret) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(secret));
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey secret) {
        return NimbusJwtDecoder.withSecretKey(secret).macAlgorithm(MacAlgorithm.HS256).build();
    }
}
