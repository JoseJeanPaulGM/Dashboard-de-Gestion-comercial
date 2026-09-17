package com.gestioncomercial.api.support.postgresql;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class PostgreSqlTestConfiguration {

    private static final String POSTGRES_IMAGE = "postgres:17-alpine";

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresqlContainer() {
        return new PostgreSQLContainer<>(POSTGRES_IMAGE)
                .withDatabaseName("gestion_comercial_test")
                .withUsername("test")
                .withPassword("test");
    }
}
