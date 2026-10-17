package com.gestioncomercial.api.sale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestioncomercial.api.inventory.application.InsufficientStockException;
import com.gestioncomercial.api.sale.application.SaleService;
import com.gestioncomercial.api.sale.domain.SaleStateConflictException;
import com.gestioncomercial.api.support.postgresql.PostgreSqlTestConfiguration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(PostgreSqlTestConfiguration.class)
@org.springframework.security.test.context.support.WithMockUser(authorities = {"SALE_READ", "SALE_WRITE", "CUSTOMER_READ", "CUSTOMER_WRITE", "CATALOG_READ", "CATALOG_WRITE"})
class SaleApiIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private SaleService saleService;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE inventory_movements, sale_lines, sales, inventory_balances, purchase_lines, purchases, supplier_products, suppliers, products, categories, customers RESTART IDENTITY");
    }

    @Test
    void createsListsConfirmsAndDecreasesStockOnce() throws Exception {
        long customerId = createCustomer();
        long first = createProduct("PEN-01", "2.50");
        long second = createProduct("PEN-02", "1.25");
        seedStock(first, 5);
        seedStock(second, 3);
        MvcResult result = createSale(customerId, """
                [{"productId":%d,"quantity":3},{"productId":%d,"quantity":2}]
                """.formatted(first, second));
        long saleId = id(result);
        assertThat(result.getResponse().getHeader("Location")).isEqualTo("http://localhost/api/v1/sales/" + saleId);
        JsonNode created = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(created.path("status").asText()).isEqualTo("DRAFT");
        assertThat(created.path("total").decimalValue()).isEqualByComparingTo("10.00");
        assertThat(created.path("lines").get(0).path("subtotal").decimalValue()).isEqualByComparingTo("7.50");
        assertThat(stock(first)).isEqualTo(5);

        mockMvc.perform(get("/api/v1/sales").param("customerId", String.valueOf(customerId))
                        .param("status", "DRAFT").param("sort", "total,desc"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
        jdbcTemplate.update("UPDATE products SET sale_price = 99.00 WHERE id = ?", first);
        mockMvc.perform(post("/api/v1/sales/{id}/confirm", saleId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.confirmedAt").exists());
        assertThat(stock(first)).isEqualTo(2);
        assertThat(stock(second)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM inventory_movements WHERE source_type = 'SALE'", Long.class))
                .isEqualTo(2);
        mockMvc.perform(get("/api/v1/sales/{id}", saleId)).andExpect(status().isOk())
                .andExpect(jsonPath("$.lines[0].unitPrice").value(2.5))
                .andExpect(jsonPath("$.total").value(10.0));
        mockMvc.perform(post("/api/v1/sales/{id}/confirm", saleId))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("SALE_STATE_CONFLICT"));
        assertThat(stock(first)).isEqualTo(2);
    }

    @Test
    void rejectsInvalidLinesUnavailablePartiesAndBadFilters() throws Exception {
        long customerId = createCustomer();
        long productId = createProduct("PEN-01", "10.00");
        mockMvc.perform(post("/api/v1/sales").contentType(MediaType.APPLICATION_JSON)
                        .content(saleJson(customerId, "[]")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mockMvc.perform(post("/api/v1/sales").contentType(MediaType.APPLICATION_JSON)
                        .content(saleJson(customerId, "[{\"productId\":" + productId + ",\"quantity\":0}]")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mockMvc.perform(post("/api/v1/sales").contentType(MediaType.APPLICATION_JSON)
                        .content(saleJson(customerId, """
                                [{"productId":%d,"quantity":1},{"productId":%d,"quantity":1}]
                                """.formatted(productId, productId))))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        mockMvc.perform(get("/api/v1/sales/999"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("SALE_NOT_FOUND"));
        mockMvc.perform(get("/api/v1/sales").param("sort", "unknown,asc"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_SORT"));
        mockMvc.perform(get("/api/v1/sales").param("size", "101"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_PAGINATION"));

        mockMvc.perform(patch("/api/v1/customers/{id}/status", customerId)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"active\":false}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/sales").contentType(MediaType.APPLICATION_JSON)
                        .content(saleJson(customerId, "[{\"productId\":" + productId + ",\"quantity\":1}]")))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("SALE_PARTY_UNAVAILABLE"));
    }

    @Test
    void keepsDraftWhenCustomerBecomesInactiveBeforeConfirmation() throws Exception {
        long customerId = createCustomer();
        long productId = createProduct("PEN-01", "10.00");
        seedStock(productId, 2);
        long saleId = id(createSale(customerId, "[{\"productId\":" + productId + ",\"quantity\":1}]"));
        mockMvc.perform(patch("/api/v1/customers/{id}/status", customerId)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"active\":false}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/sales/{id}/confirm", saleId))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("SALE_PARTY_UNAVAILABLE"));
        assertThat(stock(productId)).isEqualTo(2);
        assertThat(saleStatus(saleId)).isEqualTo("DRAFT");
    }

    @Test
    void rollsBackEarlierStockChangeWhenLaterLineIsInsufficient() throws Exception {
        long customerId = createCustomer();
        long first = createProduct("PEN-01", "2.50");
        long second = createProduct("PEN-02", "1.25");
        seedStock(first, 5);
        seedStock(second, 1);
        long saleId = id(createSale(customerId, """
                [{"productId":%d,"quantity":3},{"productId":%d,"quantity":2}]
                """.formatted(first, second)));
        mockMvc.perform(post("/api/v1/sales/{id}/confirm", saleId))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("INSUFFICIENT_STOCK"));
        assertThat(stock(first)).isEqualTo(5);
        assertThat(stock(second)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM inventory_movements", Long.class)).isZero();
        assertThat(saleStatus(saleId)).isEqualTo("DRAFT");
    }

    @Test
    void concurrentSalesCannotSpendTheSameStock() throws Exception {
        long customerId = createCustomer();
        long productId = createProduct("PEN-01", "10.00");
        seedStock(productId, 5);
        String lines = "[{\"productId\":" + productId + ",\"quantity\":4}]";
        long firstId = id(createSale(customerId, lines));
        long secondId = id(createSale(customerId, lines));
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Boolean> first = executor.submit(() -> confirmAfterSignal(start, firstId));
            Future<Boolean> second = executor.submit(() -> confirmAfterSignal(start, secondId));
            start.countDown();
            assertThat(first.get()).isNotEqualTo(second.get());
            assertThat(stock(productId)).isEqualTo(1);
            assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM inventory_movements", Long.class))
                    .isEqualTo(1);
            assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sales WHERE status = 'CONFIRMED'", Long.class))
                    .isEqualTo(1);
        } finally {
            executor.shutdownNow();
        }
    }

    private long createCustomer() throws Exception {
        return id(mockMvc.perform(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON)
                .content("{\"documentType\":\"OTHER\",\"documentNumber\":\"C-01\",\"name\":\"Customer\"}"))
                .andExpect(status().isCreated()).andReturn());
    }

    private long createProduct(String sku, String price) throws Exception {
        long categoryId = id(mockMvc.perform(post("/api/v1/categories").contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"CAT-" + sku + "\",\"name\":\"Category " + sku + "\"}"))
                .andExpect(status().isCreated()).andReturn());
        return id(mockMvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON)
                .content("{\"sku\":\"" + sku + "\",\"name\":\"Product " + sku
                        + "\",\"salePrice\":" + price + ",\"categoryId\":" + categoryId + "}"))
                .andExpect(status().isCreated()).andReturn());
    }

    private void seedStock(long productId, long quantity) {
        jdbcTemplate.update("INSERT INTO inventory_balances (product_id, quantity, updated_at) VALUES (?, ?, ?)",
                productId, quantity, OffsetDateTime.now(ZoneOffset.UTC));
    }

    private MvcResult createSale(long customerId, String lines) throws Exception {
        return mockMvc.perform(post("/api/v1/sales").contentType(MediaType.APPLICATION_JSON)
                .content(saleJson(customerId, lines))).andExpect(status().isCreated())
                .andExpect(header().exists("Location")).andReturn();
    }

    private String saleJson(long customerId, String lines) {
        return "{\"customerId\":" + customerId + ",\"lines\":" + lines + "}";
    }

    private long id(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("id").asLong();
    }

    private long stock(long productId) {
        return jdbcTemplate.query("SELECT quantity FROM inventory_balances WHERE product_id = ?",
                result -> result.next() ? result.getLong(1) : 0L, productId);
    }

    private String saleStatus(long saleId) {
        return jdbcTemplate.queryForObject("SELECT status FROM sales WHERE id = ?", String.class, saleId);
    }

    private boolean confirmAfterSignal(CountDownLatch start, long saleId) throws InterruptedException {
        start.await();
        try {
            saleService.confirm(saleId);
            return true;
        } catch (InsufficientStockException | SaleStateConflictException exception) {
            return false;
        }
    }
}
