package com.gestioncomercial.api.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    OpenAPI gestionComercialOpenApi(ApplicationProperties applicationProperties) {
        return new OpenAPI()
                .info(new Info()
                        .title(applicationProperties.name())
                        .version(applicationProperties.version())
                        .description("REST API for the Gestión Comercial application."));
    }
}
