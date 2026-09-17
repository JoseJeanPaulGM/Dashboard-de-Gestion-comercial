package com.gestioncomercial.api.shared.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "application")
public record ApplicationProperties(
        @NotBlank String name,
        @NotBlank String version
) {
}
