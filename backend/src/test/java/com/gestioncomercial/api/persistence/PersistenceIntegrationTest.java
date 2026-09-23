package com.gestioncomercial.api.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gestioncomercial.api.support.postgresql.PostgreSqlTestConfiguration;
import java.sql.Connection;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@ActiveProfiles("test")
@Import(PostgreSqlTestConfiguration.class)
class PersistenceIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE supplier_products, suppliers, products, categories, customers RESTART IDENTITY");
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
    void appliesAllFlywayMigrationsAndCreatesTheExpectedTables() {
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("3");

        Integer tableCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM information_schema.tables
                WHERE table_schema = 'public'
                  AND table_name IN ('customers', 'categories', 'products', 'suppliers', 'supplier_products')
                """,
                Integer.class
        );
        assertThat(tableCount).isEqualTo(5);
    }

    @Test
    void enforcesTheCustomerDocumentUniqueConstraint() {
        insertCustomer("RUC", "20123456789", "First customer");

        assertThatThrownBy(() -> insertCustomer("RUC", "20123456789", "Second customer"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rollsBackAllWritesWhenATransactionFails() {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);

        assertThatThrownBy(() -> transaction.executeWithoutResult(status -> {
            insertCustomer("DNI", "12345678", "First customer");
            insertCustomer("DNI", "87654321", "Second customer");
            throw new IllegalStateException("Intentional failure");
        })).isInstanceOf(IllegalStateException.class);

        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM customers", Long.class)).isZero();
    }

    @Test
    void enforcesCatalogUniquenessRelationshipAndPositivePrice() {
        long categoryId = insertCategory("OFFICE", "Office");
        insertProduct("PEN-01", "Pen", "2.50", categoryId);

        assertThatThrownBy(() -> insertCategory("OFFICE", "Duplicate"))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insertProduct("PEN-01", "Duplicate", "3.00", categoryId))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insertProduct("PEN-02", "Invalid category", "3.00", 999))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insertProduct("PEN-03", "Invalid price", "0.00", categoryId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void enforcesSupplierUniquenessRelationshipsAndRucFormat() {
        long categoryId = insertCategory("OFFICE", "Office");
        long productId = insertProductReturningId("PEN-01", "Pen", "2.50", categoryId);
        long supplierId = insertSupplier("20123456789", "Acme");
        insertSupplierProduct(supplierId, productId);

        assertThatThrownBy(() -> insertSupplier("20123456789", "Duplicate"))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insertSupplier("ABC", "Invalid"))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insertSupplierProduct(supplierId, productId))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insertSupplierProduct(supplierId, 999))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private void insertCustomer(String type, String number, String name) {
        OffsetDateTime now = OffsetDateTime.ofInstant(
                Instant.parse("2026-09-18T12:00:00Z"),
                ZoneOffset.UTC
        );
        jdbcTemplate.update(
                """
                INSERT INTO customers (
                    document_type, document_number, name, active, created_at, updated_at
                ) VALUES (?, ?, ?, TRUE, ?, ?)
                """,
                type,
                number,
                name,
                now,
                now
        );
    }

    private long insertCategory(String code, String name) {
        OffsetDateTime now = OffsetDateTime.ofInstant(Instant.parse("2026-09-18T12:00:00Z"), ZoneOffset.UTC);
        return jdbcTemplate.queryForObject(
                """
                INSERT INTO categories (code, name, active, created_at, updated_at)
                VALUES (?, ?, TRUE, ?, ?) RETURNING id
                """,
                Long.class,
                code,
                name,
                now,
                now
        );
    }

    private void insertProduct(String sku, String name, String price, long categoryId) {
        OffsetDateTime now = OffsetDateTime.ofInstant(Instant.parse("2026-09-18T12:00:00Z"), ZoneOffset.UTC);
        jdbcTemplate.update(
                """
                INSERT INTO products (sku, name, sale_price, category_id, active, created_at, updated_at)
                VALUES (?, ?, CAST(? AS NUMERIC), ?, TRUE, ?, ?)
                """,
                sku,
                name,
                price,
                categoryId,
                now,
                now
        );
    }

    private long insertProductReturningId(String sku, String name, String price, long categoryId) {
        OffsetDateTime now = OffsetDateTime.ofInstant(Instant.parse("2026-09-18T12:00:00Z"), ZoneOffset.UTC);
        return jdbcTemplate.queryForObject(
                """
                INSERT INTO products (sku, name, sale_price, category_id, active, created_at, updated_at)
                VALUES (?, ?, CAST(? AS NUMERIC), ?, TRUE, ?, ?) RETURNING id
                """, Long.class, sku, name, price, categoryId, now, now);
    }

    private long insertSupplier(String ruc, String name) {
        OffsetDateTime now = OffsetDateTime.ofInstant(Instant.parse("2026-09-18T12:00:00Z"), ZoneOffset.UTC);
        return jdbcTemplate.queryForObject(
                """
                INSERT INTO suppliers (ruc, business_name, active, created_at, updated_at)
                VALUES (?, ?, TRUE, ?, ?) RETURNING id
                """, Long.class, ruc, name, now, now);
    }

    private void insertSupplierProduct(long supplierId, long productId) {
        OffsetDateTime now = OffsetDateTime.ofInstant(Instant.parse("2026-09-18T12:00:00Z"), ZoneOffset.UTC);
        jdbcTemplate.update(
                """
                INSERT INTO supplier_products (supplier_id, product_id, active, created_at, updated_at)
                VALUES (?, ?, TRUE, ?, ?)
                """, supplierId, productId, now, now);
    }
}
