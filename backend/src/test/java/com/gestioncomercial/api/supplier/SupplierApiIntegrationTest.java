package com.gestioncomercial.api.supplier;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestioncomercial.api.support.postgresql.PostgreSqlTestConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
class SupplierApiIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE supplier_products, suppliers, products, categories, customers RESTART IDENTITY");
    }

    @Test
    void createsReadsAndUpdatesSupplier() throws Exception {
        MvcResult creation = mockMvc.perform(post("/api/v1/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(supplierJson("20123456789", " Acme Peru ", "Acme")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/suppliers/1"))
                .andExpect(jsonPath("$.ruc").value("20123456789"))
                .andExpect(jsonPath("$.businessName").value("Acme Peru"))
                .andReturn();

        long id = id(creation);
        mockMvc.perform(get("/api/v1/suppliers/{id}", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.tradeName").value("Acme"));
        mockMvc.perform(put("/api/v1/suppliers/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(supplierJson("20987654321", "Acme Updated", null)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.ruc").value("20987654321"))
                .andExpect(jsonPath("$.tradeName").doesNotExist());
    }

    @Test
    void validatesRucEmailUniquenessAndMissingSupplier() throws Exception {
        createSupplier("20123456789", "Acme");
        mockMvc.perform(post("/api/v1/suppliers").contentType(MediaType.APPLICATION_JSON)
                        .content(supplierJson("123", "Invalid", null)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mockMvc.perform(post("/api/v1/suppliers").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ruc\":\"20111111111\",\"businessName\":\"Invalid\",\"email\":\"bad\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mockMvc.perform(post("/api/v1/suppliers").contentType(MediaType.APPLICATION_JSON)
                        .content(supplierJson("20123456789", "Duplicate", null)))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("SUPPLIER_RUC_CONFLICT"));
        mockMvc.perform(get("/api/v1/suppliers/999")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SUPPLIER_NOT_FOUND"));
    }

    @Test
    void linksListsAndUpdatesSupplierProducts() throws Exception {
        long supplierId = createSupplier("20123456789", "Acme");
        long productId = createProduct("PEN-01", "Blue pen");

        mockMvc.perform(post("/api/v1/suppliers/{id}/products", supplierId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + productId + ",\"supplierProductCode\":\" AC-PEN \"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/suppliers/1/products/1"))
                .andExpect(jsonPath("$.sku").value("PEN-01"))
                .andExpect(jsonPath("$.supplierProductCode").value("AC-PEN"));

        mockMvc.perform(get("/api/v1/suppliers/{id}/products", supplierId).param("search", "blue"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
        mockMvc.perform(put("/api/v1/suppliers/{supplierId}/products/{productId}", supplierId, productId)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"supplierProductCode\":\"NEW-CODE\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.supplierProductCode").value("NEW-CODE"));
    }

    @Test
    void enforcesSupplierProductLifecycle() throws Exception {
        long supplierId = createSupplier("20123456789", "Acme");
        long productId = createProduct("PEN-01", "Pen");
        linkProduct(supplierId, productId);

        mockMvc.perform(patch("/api/v1/suppliers/{id}/status", supplierId)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"active\":false}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("SUPPLIER_HAS_ACTIVE_PRODUCTS"));
        mockMvc.perform(patch("/api/v1/products/{id}/status", productId)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"active\":false}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PRODUCT_HAS_ACTIVE_SUPPLIERS"));
        changeLinkStatus(supplierId, productId, false);
        changeSupplierStatus(supplierId, false);
        mockMvc.perform(patch("/api/v1/suppliers/{supplierId}/products/{productId}/status", supplierId, productId)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"active\":true}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("SUPPLIER_PRODUCT_INACTIVE_PARTY"));
        changeSupplierStatus(supplierId, true);
        changeProductStatus(productId, false);
        mockMvc.perform(get("/api/v1/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
        mockMvc.perform(patch("/api/v1/suppliers/{supplierId}/products/{productId}/status", supplierId, productId)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"active\":true}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("SUPPLIER_PRODUCT_INACTIVE_PARTY"));
    }

    @Test
    void rejectsDuplicateAndMissingProductLinks() throws Exception {
        long supplierId = createSupplier("20123456789", "Acme");
        long productId = createProduct("PEN-01", "Pen");
        linkProduct(supplierId, productId);
        mockMvc.perform(post("/api/v1/suppliers/{id}/products", supplierId)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"productId\":" + productId + "}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("SUPPLIER_PRODUCT_CONFLICT"));
        mockMvc.perform(put("/api/v1/suppliers/{supplierId}/products/{productId}", supplierId, 999)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"supplierProductCode\":null}"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("SUPPLIER_PRODUCT_NOT_FOUND"));
    }

    @Test
    void filtersSearchesAndPaginatesSuppliersLiterally() throws Exception {
        long inactive = createSupplier("20123456789", "Dormant");
        createSupplier("20987654321", "100% Supplies");
        changeSupplierStatus(inactive, false);
        mockMvc.perform(get("/api/v1/suppliers").param("active", "false"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
        mockMvc.perform(get("/api/v1/suppliers").param("search", "%"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
        mockMvc.perform(get("/api/v1/suppliers").param("size", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalPages").value(2));
    }

    @ParameterizedTest
    @ValueSource(strings = {"id", "ruc", "businessName", "tradeName", "active", "createdAt", "updatedAt"})
    void acceptsEverySupplierSortField(String field) throws Exception {
        createSupplier("20123456789", "Acme");
        mockMvc.perform(get("/api/v1/suppliers").param("sort", field + ",desc"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
    }

    @ParameterizedTest
    @ValueSource(strings = {"id", "sku", "name", "supplierProductCode", "active", "createdAt", "updatedAt"})
    void acceptsEverySupplierProductSortField(String field) throws Exception {
        long supplierId = createSupplier("20123456789", "Acme");
        long productId = createProduct("PEN-01", "Pen");
        linkProduct(supplierId, productId);
        mockMvc.perform(get("/api/v1/suppliers/{id}/products", supplierId).param("sort", field + ",desc"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
    }

    private long createSupplier(String ruc, String name) throws Exception {
        return id(mockMvc.perform(post("/api/v1/suppliers").contentType(MediaType.APPLICATION_JSON)
                .content(supplierJson(ruc, name, null))).andExpect(status().isCreated()).andReturn());
    }

    private long createProduct(String sku, String name) throws Exception {
        MvcResult category = mockMvc.perform(post("/api/v1/categories").contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"CAT-" + sku + "\",\"name\":\"Category " + sku + "\"}"))
                .andExpect(status().isCreated()).andReturn();
        long categoryId = id(category);
        return id(mockMvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON)
                .content("{\"sku\":\"" + sku + "\",\"name\":\"" + name
                        + "\",\"salePrice\":10.00,\"categoryId\":" + categoryId + "}"))
                .andExpect(status().isCreated()).andReturn());
    }

    private void linkProduct(long supplierId, long productId) throws Exception {
        mockMvc.perform(post("/api/v1/suppliers/{id}/products", supplierId).contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\":" + productId + "}")).andExpect(status().isCreated());
    }

    private void changeSupplierStatus(long supplierId, boolean active) throws Exception {
        mockMvc.perform(patch("/api/v1/suppliers/{id}/status", supplierId).contentType(MediaType.APPLICATION_JSON)
                .content("{\"active\":" + active + "}")).andExpect(status().isOk());
    }

    private void changeProductStatus(long productId, boolean active) throws Exception {
        mockMvc.perform(patch("/api/v1/products/{id}/status", productId).contentType(MediaType.APPLICATION_JSON)
                .content("{\"active\":" + active + "}")).andExpect(status().isOk());
    }

    private void changeLinkStatus(long supplierId, long productId, boolean active) throws Exception {
        mockMvc.perform(patch("/api/v1/suppliers/{supplierId}/products/{productId}/status", supplierId, productId)
                .contentType(MediaType.APPLICATION_JSON).content("{\"active\":" + active + "}"))
                .andExpect(status().isOk());
    }

    private long id(MvcResult result) throws Exception {
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("id").asLong();
    }

    private String supplierJson(String ruc, String name, String tradeName) {
        String tradeNameJson = tradeName == null ? "null" : "\"" + tradeName + "\"";
        return """
                {"ruc":"%s","businessName":"%s","tradeName":%s,
                 "email":"contact@example.test","phone":"999000111","address":"Lima"}
                """.formatted(ruc, name, tradeNameJson);
    }
}
