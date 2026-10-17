package com.gestioncomercial.api.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gestioncomercial.api.inventory.application.InventoryService;
import com.gestioncomercial.api.support.postgresql.PostgreSqlTestConfiguration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(PostgreSqlTestConfiguration.class)
@org.springframework.security.test.context.support.WithMockUser(authorities = {"INVENTORY_READ", "INVENTORY_ADJUST"})
class InventoryApiIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private InventoryService inventory;

    @BeforeEach
    void cleanDatabase() {
        jdbc.execute("TRUNCATE TABLE inventory_movements, sale_lines, sales, inventory_balances, "
                + "purchase_lines, purchases, supplier_products, suppliers, products, categories, customers "
                + "RESTART IDENTITY");
    }

    @Test
    void adjustmentsAreIdempotentAndAlertsReflectTheMinimum() throws Exception {
        long productId = createProduct();
        mockMvc.perform(get("/api/v1/inventory/balances/{id}", productId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.quantity").value(0))
                .andExpect(jsonPath("$.lowStock").value(false));
        mockMvc.perform(put("/api/v1/inventory/balances/{id}/minimum", productId)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"minimumQuantity\":5}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.minimumQuantity").value(5))
                .andExpect(jsonPath("$.lowStock").value(true));
        mockMvc.perform(get("/api/v1/inventory/alerts"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));

        UUID firstKey = UUID.randomUUID();
        String first = adjustment(productId, 10, "Initial count", firstKey);
        mockMvc.perform(post("/api/v1/inventory/adjustments")
                        .contentType(MediaType.APPLICATION_JSON).content(first))
                .andExpect(status().isOk()).andExpect(jsonPath("$.balanceAfter").value(10))
                .andExpect(jsonPath("$.sourceType").value("ADJUSTMENT"));
        mockMvc.perform(post("/api/v1/inventory/adjustments")
                        .contentType(MediaType.APPLICATION_JSON).content(first))
                .andExpect(status().isOk()).andExpect(jsonPath("$.balanceAfter").value(10));
        assertThat(movementCount()).isEqualTo(1);
        mockMvc.perform(get("/api/v1/inventory/alerts"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(0));

        mockMvc.perform(post("/api/v1/inventory/adjustments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adjustment(productId, -6, "Damaged units", UUID.randomUUID())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.balanceAfter").value(4));
        mockMvc.perform(get("/api/v1/inventory/alerts"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].productId").value(productId));
        mockMvc.perform(get("/api/v1/inventory/balances/{id}/movements", productId)
                        .param("size", "1").param("sort", "id,desc"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content[0].quantityChange").value(-6));
        mockMvc.perform(post("/api/v1/inventory/adjustments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adjustment(productId, 9, "Different count", firstKey)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ADJUSTMENT_KEY_CONFLICT"));
        mockMvc.perform(post("/api/v1/inventory/adjustments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adjustment(productId, -5, "Damaged units", UUID.randomUUID())))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("INSUFFICIENT_STOCK"));
        assertThat(movementCount()).isEqualTo(2);
        assertThat(jdbc.queryForObject("SELECT quantity FROM inventory_balances WHERE product_id = ?",
                Long.class, productId)).isEqualTo(4);
        assertThat(jdbc.queryForObject("""
                SELECT SUM(quantity_change) FROM inventory_movements WHERE product_id = ?
                """, Long.class, productId)).isEqualTo(4);
    }

    @Test
    void concurrentRetriesWithTheSameKeyCreateOneMovement() throws Exception {
        long productId = createProduct();
        UUID key = UUID.randomUUID();
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Long> first = executor.submit(() -> {
                start.await();
                return inventory.adjust(productId, 7, "Initial count", key).id();
            });
            Future<Long> second = executor.submit(() -> {
                start.await();
                return inventory.adjust(productId, 7, "Initial count", key).id();
            });
            start.countDown();
            assertThat(first.get()).isEqualTo(second.get());
            assertThat(movementCount()).isEqualTo(1);
            assertThat(jdbc.queryForObject("SELECT quantity FROM inventory_balances WHERE product_id = ?",
                    Long.class, productId)).isEqualTo(7);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void rejectsInvalidInputAndUnknownProducts() throws Exception {
        long productId = createProduct();
        mockMvc.perform(put("/api/v1/inventory/balances/{id}/minimum", productId)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"minimumQuantity\":-1}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mockMvc.perform(post("/api/v1/inventory/adjustments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adjustment(productId, 0, "Initial count", UUID.randomUUID())))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        mockMvc.perform(get("/api/v1/inventory/balances/999"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
        mockMvc.perform(get("/api/v1/inventory/balances/{id}/movements", productId)
                        .param("sort", "unknown,asc"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_SORT"));
        mockMvc.perform(get("/api/v1/inventory/alerts").param("size", "101"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_PAGINATION"));
    }

    private long createProduct() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        long categoryId = jdbc.queryForObject("""
                INSERT INTO categories (code, name, active, created_at, updated_at)
                VALUES ('INVENTORY', 'Inventory', TRUE, ?, ?) RETURNING id
                """, Long.class, now, now);
        return jdbc.queryForObject("""
                INSERT INTO products (sku, name, sale_price, category_id, active, created_at, updated_at)
                VALUES ('INV-01', 'Inventory product', 1.00, ?, TRUE, ?, ?) RETURNING id
                """, Long.class, categoryId, now, now);
    }

    private String adjustment(long productId, long change, String reason, UUID key) {
        return "{\"productId\":" + productId + ",\"quantityChange\":" + change
                + ",\"reason\":\"" + reason + "\",\"requestKey\":\"" + key + "\"}";
    }

    private long movementCount() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM inventory_movements", Long.class);
    }
}
