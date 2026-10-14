package com.gestioncomercial.api.purchase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestioncomercial.api.purchase.application.PurchaseService;
import com.gestioncomercial.api.purchase.domain.PurchaseStateConflictException;
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
class PurchaseApiIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private PurchaseService purchaseService;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE sale_lines, sales, inventory_balances, purchase_lines, purchases, supplier_products, suppliers, products, categories, customers RESTART IDENTITY");
    }

    @Test
    void createsListsConfirmsAndIncreasesStockOnce() throws Exception {
        long supplierId = createSupplier();
        long first = createProduct("PEN-01");
        long second = createProduct("PEN-02");
        link(supplierId, first);
        link(supplierId, second);

        MvcResult result = createPurchase(supplierId, """
                [{"productId":%d,"quantity":3,"unitPrice":2.50},
                 {"productId":%d,"quantity":2,"unitPrice":1.25}]
                """.formatted(first, second));
        long id = id(result);
        assertThat(result.getResponse().getHeader("Location")).isEqualTo("http://localhost/api/v1/purchases/" + id);
        JsonNode created = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(created.path("status").asText()).isEqualTo("DRAFT");
        assertThat(created.path("total").decimalValue()).isEqualByComparingTo("10.00");
        assertThat(created.path("lines").get(0).path("subtotal").decimalValue()).isEqualByComparingTo("7.50");
        assertThat(stock(first)).isZero();

        mockMvc.perform(get("/api/v1/purchases/{id}", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.lines.length()").value(2));
        mockMvc.perform(get("/api/v1/purchases").param("supplierId", String.valueOf(supplierId))
                        .param("status", "DRAFT").param("sort", "total,desc"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].total").value(10.0));

        mockMvc.perform(post("/api/v1/purchases/{id}/confirm", id))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.confirmedAt").exists());
        assertThat(stock(first)).isEqualTo(3);
        assertThat(stock(second)).isEqualTo(2);
        jdbcTemplate.update("UPDATE products SET sale_price = 99.00 WHERE id = ?", first);
        mockMvc.perform(get("/api/v1/purchases/{id}", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.lines[0].unitPrice").value(2.5))
                .andExpect(jsonPath("$.total").value(10.0));
        mockMvc.perform(post("/api/v1/purchases/{id}/confirm", id))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("PURCHASE_STATE_CONFLICT"));
        assertThat(stock(first)).isEqualTo(3);
        assertThat(stock(second)).isEqualTo(2);
        mockMvc.perform(get("/api/v1/purchases").param("status", "CONFIRMED"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void rejectsMalformedLinesAndDuplicateProducts() throws Exception {
        long supplierId = createSupplier();
        long productId = createProduct("PEN-01");
        link(supplierId, productId);
        mockMvc.perform(post("/api/v1/purchases").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"supplierId\":" + supplierId + ",\"lines\":[]}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mockMvc.perform(post("/api/v1/purchases").contentType(MediaType.APPLICATION_JSON)
                        .content(purchaseJson(supplierId,
                                "[{\"productId\":" + productId + ",\"quantity\":0,\"unitPrice\":2.50}]")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mockMvc.perform(post("/api/v1/purchases").contentType(MediaType.APPLICATION_JSON)
                        .content(purchaseJson(supplierId,
                                "[{\"productId\":" + productId + ",\"quantity\":1,\"unitPrice\":2.501}]")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mockMvc.perform(post("/api/v1/purchases").contentType(MediaType.APPLICATION_JSON)
                        .content(purchaseJson(supplierId, """
                                [{"productId":%d,"quantity":1,"unitPrice":2.50},
                                 {"productId":%d,"quantity":1,"unitPrice":2.50}]
                                """.formatted(productId, productId))))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM purchases", Long.class)).isZero();
    }

    @Test
    void requiresActiveSupplierProductLinkAtCreationAndConfirmation() throws Exception {
        long supplierId = createSupplier();
        long productId = createProduct("PEN-01");
        String lines = "[{\"productId\":" + productId + ",\"quantity\":1,\"unitPrice\":2.50}]";
        mockMvc.perform(post("/api/v1/purchases").contentType(MediaType.APPLICATION_JSON)
                        .content(purchaseJson(supplierId, lines)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PURCHASE_SUPPLY_UNAVAILABLE"));
        link(supplierId, productId);
        long purchaseId = id(createPurchase(supplierId, lines));
        mockMvc.perform(patch("/api/v1/suppliers/{supplierId}/products/{productId}/status", supplierId, productId)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"active\":false}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/purchases/{id}/confirm", purchaseId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PURCHASE_SUPPLY_UNAVAILABLE"));
        assertThat(stock(productId)).isZero();
        mockMvc.perform(get("/api/v1/purchases/{id}", purchaseId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void rollsBackEveryStockChangeWhenConfirmationFails() throws Exception {
        long supplierId = createSupplier();
        long first = createProduct("PEN-01");
        long second = createProduct("PEN-02");
        link(supplierId, first);
        link(supplierId, second);
        long purchaseId = id(createPurchase(supplierId, """
                [{"productId":%d,"quantity":1,"unitPrice":1.00},
                 {"productId":%d,"quantity":1,"unitPrice":1.00}]
                """.formatted(first, second)));
        jdbcTemplate.update("INSERT INTO inventory_balances (product_id, quantity, updated_at) VALUES (?, ?, ?)",
                second, Long.MAX_VALUE, OffsetDateTime.now(ZoneOffset.UTC));

        mockMvc.perform(post("/api/v1/purchases/{id}/confirm", purchaseId))
                .andExpect(status().isInternalServerError());
        assertThat(stock(first)).isZero();
        assertThat(stock(second)).isEqualTo(Long.MAX_VALUE);
        mockMvc.perform(get("/api/v1/purchases/{id}", purchaseId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void concurrentConfirmationsIncreaseStockOnlyOnce() throws Exception {
        long supplierId = createSupplier();
        long productId = createProduct("PEN-01");
        link(supplierId, productId);
        long purchaseId = id(createPurchase(supplierId,
                "[{\"productId\":" + productId + ",\"quantity\":4,\"unitPrice\":2.50}]"));
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Boolean> first = executor.submit(() -> confirmAfterSignal(start, purchaseId));
            Future<Boolean> second = executor.submit(() -> confirmAfterSignal(start, purchaseId));
            start.countDown();
            assertThat(first.get()).isNotEqualTo(second.get());
            assertThat(stock(productId)).isEqualTo(4);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void reportsMissingPurchaseAndInvalidFilters() throws Exception {
        mockMvc.perform(get("/api/v1/purchases/999"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("PURCHASE_NOT_FOUND"));
        mockMvc.perform(get("/api/v1/purchases").param("sort", "unknown,asc"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_SORT"));
        mockMvc.perform(get("/api/v1/purchases").param("size", "101"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_PAGINATION"));
    }

    private long createSupplier() throws Exception {
        return id(mockMvc.perform(post("/api/v1/suppliers").contentType(MediaType.APPLICATION_JSON)
                .content("{\"ruc\":\"20123456789\",\"businessName\":\"Acme\"}"))
                .andExpect(status().isCreated()).andReturn());
    }

    private long createProduct(String sku) throws Exception {
        long categoryId = id(mockMvc.perform(post("/api/v1/categories").contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"CAT-" + sku + "\",\"name\":\"Category " + sku + "\"}"))
                .andExpect(status().isCreated()).andReturn());
        return id(mockMvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON)
                .content("{\"sku\":\"" + sku + "\",\"name\":\"Product " + sku
                        + "\",\"salePrice\":10.00,\"categoryId\":" + categoryId + "}"))
                .andExpect(status().isCreated()).andReturn());
    }

    private void link(long supplierId, long productId) throws Exception {
        mockMvc.perform(post("/api/v1/suppliers/{id}/products", supplierId).contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\":" + productId + "}"))
                .andExpect(status().isCreated());
    }

    private MvcResult createPurchase(long supplierId, String lines) throws Exception {
        return mockMvc.perform(post("/api/v1/purchases").contentType(MediaType.APPLICATION_JSON)
                .content(purchaseJson(supplierId, lines))).andExpect(status().isCreated())
                .andExpect(header().exists("Location")).andReturn();
    }

    private String purchaseJson(long supplierId, String lines) {
        return "{\"supplierId\":" + supplierId + ",\"lines\":" + lines + "}";
    }

    private long id(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("id").asLong();
    }

    private long stock(long productId) {
        Long quantity = jdbcTemplate.query("SELECT quantity FROM inventory_balances WHERE product_id = ?",
                result -> result.next() ? result.getLong(1) : 0L, productId);
        return quantity;
    }

    private boolean confirmAfterSignal(CountDownLatch start, long purchaseId) throws InterruptedException {
        start.await();
        try {
            purchaseService.confirm(purchaseId);
            return true;
        } catch (PurchaseStateConflictException exception) {
            return false;
        }
    }
}
