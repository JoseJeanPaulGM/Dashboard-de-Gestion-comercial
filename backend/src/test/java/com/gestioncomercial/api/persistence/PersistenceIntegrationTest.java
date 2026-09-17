package com.gestioncomercial.api.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import com.gestioncomercial.api.support.postgresql.PostgreSqlTestConfiguration;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(PostgreSqlTestConfiguration.class)
class PersistenceIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PersistenceProbeRepository repository;

    @Autowired
    private TransactionProbeService transactionProbeService;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    @Test
    void connectsToPostgreSql17() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection.isValid(2)).isTrue();
            assertThat(connection.getMetaData().getDatabaseProductName()).isEqualTo("PostgreSQL");
            assertThat(connection.getMetaData().getDatabaseMajorVersion()).isEqualTo(17);
        }
    }

    @Test
    void persistsAndReadsAnEntityWithGeneratedLongIdAndUtcInstant() {
        Instant createdAt = Instant.parse("2026-09-17T08:00:00.123456Z")
                .truncatedTo(ChronoUnit.MICROS);

        PersistenceProbe saved = repository.saveAndFlush(new PersistenceProbe("jpa-probe", createdAt));

        assertThat(saved.getId()).isNotNull().isPositive();
        assertThat(repository.findById(saved.getId()))
                .hasValueSatisfying(found -> {
                    assertThat(found.getValue()).isEqualTo("jpa-probe");
                    assertThat(found.getCreatedAt()).isEqualTo(createdAt);
                });
    }

    @Test
    void enforcesDatabaseNotNullConstraint() {
        assertThatThrownBy(() -> repository.saveAndFlush(new PersistenceProbe(null, Instant.now())))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void commitsAllWritesFromSuccessfulTransaction() {
        transactionProbeService.savePair("first", "second");

        assertThat(repository.count()).isEqualTo(2);
    }

    @Test
    void rollsBackAllWritesWhenTransactionFails() {
        assertThatThrownBy(() -> transactionProbeService.savePairAndFail("first", "second"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Intentional failure");

        assertThat(repository.count()).isZero();
    }
}
