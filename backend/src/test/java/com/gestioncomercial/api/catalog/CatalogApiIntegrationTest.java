package com.gestioncomercial.api.catalog;

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
class CatalogApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE supplier_products, suppliers, products, categories RESTART IDENTITY");
    }

    @Test
    void createsReadsAndUpdatesANormalizedCategory() throws Exception {
        MvcResult creation = mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryJson(" office ", " Office supplies ", " General ")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/categories/1"))
                .andExpect(jsonPath("$.code").value("OFFICE"))
                .andExpect(jsonPath("$.name").value("Office supplies"))
                .andExpect(jsonPath("$.active").value(true))
                .andReturn();

        long id = id(creation);
        mockMvc.perform(get("/api/v1/categories/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("General"));

        mockMvc.perform(put("/api/v1/categories/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryJson("stationery", "Stationery", "Updated")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("STATIONERY"))
                .andExpect(jsonPath("$.name").value("Stationery"));
    }

    @Test
    void validatesCategoryInputAndUniqueness() throws Exception {
        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\" \",\"name\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        createCategory("OFFICE", "Office");
        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryJson(" office ", "Other", null)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CATEGORY_CODE_CONFLICT"));

        mockMvc.perform(get("/api/v1/categories/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CATEGORY_NOT_FOUND"));
    }

    @Test
    void paginatesFiltersAndSearchesCategoriesLiterally() throws Exception {
        long inactiveId = createCategory("OLD", "Dormant");
        createCategory("OFFICE", "Office");
        createCategory("PERCENT", "100% real");
        changeCategoryStatus(inactiveId, false);

        mockMvc.perform(get("/api/v1/categories").param("active", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].code").value("OLD"));
        mockMvc.perform(get("/api/v1/categories").param("search", "off"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
        mockMvc.perform(get("/api/v1/categories").param("search", "%"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].code").value("PERCENT"));
        mockMvc.perform(get("/api/v1/categories").param("size", "2").param("sort", "code,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void createsReadsAndUpdatesAProductInPen() throws Exception {
        long officeId = createCategory("OFFICE", "Office");
        long technologyId = createCategory("TECH", "Technology");
        MvcResult creation = mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson(" pen-01 ", " Blue pen ", "2.50", officeId)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/products/1"))
                .andExpect(jsonPath("$.sku").value("PEN-01"))
                .andExpect(jsonPath("$.salePrice").value(2.50))
                .andExpect(jsonPath("$.currency").value("PEN"))
                .andExpect(jsonPath("$.category.code").value("OFFICE"))
                .andReturn();

        long id = id(creation);
        mockMvc.perform(get("/api/v1/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Blue pen"));
        mockMvc.perform(put("/api/v1/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("LAP-01", "Laptop", "3499.90", technologyId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category.code").value("TECH"))
                .andExpect(jsonPath("$.salePrice").value(3499.90));
    }

    @Test
    void validatesProductPriceSkuAndCategory() throws Exception {
        long categoryId = createCategory("OFFICE", "Office");
        createProduct("PEN-01", "Pen", "2.50", categoryId);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("PEN-02", "Invalid", "0", categoryId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("PEN-02", "Invalid", "2.555", categoryId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson(" pen-01 ", "Duplicate", "3.00", categoryId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PRODUCT_SKU_CONFLICT"));
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("PEN-02", "Unknown", "3.00", 999)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CATEGORY_NOT_FOUND"));
        mockMvc.perform(get("/api/v1/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
    }

    @Test
    void enforcesCategoryAndProductLifecycleRules() throws Exception {
        long categoryId = createCategory("OFFICE", "Office");
        long productId = createProduct("PEN-01", "Pen", "2.50", categoryId);

        mockMvc.perform(patch("/api/v1/categories/{id}/status", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":false}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CATEGORY_HAS_ACTIVE_PRODUCTS"));

        changeProductStatus(productId, false);
        changeCategoryStatus(categoryId, false);
        mockMvc.perform(patch("/api/v1/products/{id}/status", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":true}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PRODUCT_CATEGORY_INACTIVE"));
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("PEN-02", "Other", "3.00", categoryId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PRODUCT_CATEGORY_INACTIVE"));
    }

    @Test
    void filtersAndPaginatesProducts() throws Exception {
        long officeId = createCategory("OFFICE", "Office");
        long technologyId = createCategory("TECH", "Technology");
        createProduct("PEN-01", "Blue pen", "2.50", officeId);
        createProduct("PERCENT", "100% keyboard", "120.00", technologyId);
        long inactiveId = createProduct("LAP-01", "Laptop", "3500.00", technologyId);
        changeProductStatus(inactiveId, false);

        mockMvc.perform(get("/api/v1/products")
                        .param("categoryId", String.valueOf(technologyId))
                        .param("active", "true")
                        .param("minPrice", "100.00")
                        .param("maxPrice", "200.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].sku").value("PERCENT"));
        mockMvc.perform(get("/api/v1/products").param("search", "%"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
        mockMvc.perform(get("/api/v1/products").param("page", "9").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(3));
        mockMvc.perform(get("/api/v1/products").param("minPrice", "200").param("maxPrice", "100"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"id", "sku", "name", "salePrice", "categoryName", "active", "createdAt", "updatedAt"})
    void acceptsEveryProductSortField(String field) throws Exception {
        long categoryId = createCategory("OFFICE", "Office");
        createProduct("PEN-01", "Pen", "2.50", categoryId);

        mockMvc.perform(get("/api/v1/products").param("sort", field + ",desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @ParameterizedTest
    @ValueSource(strings = {"id", "code", "name", "active", "createdAt", "updatedAt"})
    void acceptsEveryCategorySortField(String field) throws Exception {
        createCategory("OFFICE", "Office");

        mockMvc.perform(get("/api/v1/categories").param("sort", field + ",desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    private long createCategory(String code, String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryJson(code, name, null)))
                .andExpect(status().isCreated())
                .andReturn();
        return id(result);
    }

    private long createProduct(String sku, String name, String price, long categoryId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson(sku, name, price, categoryId)))
                .andExpect(status().isCreated())
                .andReturn();
        return id(result);
    }

    private void changeCategoryStatus(long id, boolean active) throws Exception {
        mockMvc.perform(patch("/api/v1/categories/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":" + active + "}"))
                .andExpect(status().isOk());
    }

    private void changeProductStatus(long id, boolean active) throws Exception {
        mockMvc.perform(patch("/api/v1/products/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":" + active + "}"))
                .andExpect(status().isOk());
    }

    private long id(MvcResult result) throws Exception {
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("id").asLong();
    }

    private String categoryJson(String code, String name, String description) {
        String descriptionJson = description == null ? "null" : "\"" + description + "\"";
        return """
                {"code":"%s","name":"%s","description":%s}
                """.formatted(code, name, descriptionJson);
    }

    private String productJson(String sku, String name, String price, long categoryId) {
        return """
                {
                  "sku":"%s",
                  "name":"%s",
                  "description":"Catalog product",
                  "salePrice":%s,
                  "categoryId":%d
                }
                """.formatted(sku, name, price, categoryId);
    }
}
