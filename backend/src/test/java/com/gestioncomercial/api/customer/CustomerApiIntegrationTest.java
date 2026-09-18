package com.gestioncomercial.api.customer;

import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
class CustomerApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE customers RESTART IDENTITY");
    }

    @Test
    void createsAndReadsANormalizedCustomer() throws Exception {
        MvcResult creation = mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Correlation-Id", "customer-create")
                        .content(customerJson("OTHER", " ab-123 ", " Example Customer ")))
                .andExpect(status().isCreated())
                .andExpect(header().string("X-Correlation-Id", "customer-create"))
                .andExpect(header().string("Location", "http://localhost/api/v1/customers/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.documentNumber").value("AB-123"))
                .andExpect(jsonPath("$.name").value("Example Customer"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andReturn();

        long id = objectMapper.readTree(creation.getResponse().getContentAsString()).get("id").asLong();
        mockMvc.perform(get("/api/v1/customers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentType").value("OTHER"))
                .andExpect(jsonPath("$.email").value("contact@example.com"));
    }

    @Test
    void rejectsInvalidInputAndDuplicateDocuments() throws Exception {
        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"documentType":null,"documentNumber":" ","name":" ","email":"invalid"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors").isArray());

        createCustomer("OTHER", "AB-123", "First");
        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerJson("OTHER", " ab-123 ", "Second")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CUSTOMER_DOCUMENT_CONFLICT"));

        long secondId = createCustomer("OTHER", "SECOND-01", "Second");
        mockMvc.perform(put("/api/v1/customers/{id}", secondId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerJson("OTHER", "AB-123", "Conflicting update")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CUSTOMER_DOCUMENT_CONFLICT"));
        mockMvc.perform(get("/api/v1/customers/{id}", secondId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentNumber").value("SECOND-01"))
                .andExpect(jsonPath("$.name").value("Second"));
    }

    @Test
    void updatesCustomerDataAndItsEditableDocument() throws Exception {
        long id = createCustomer("DNI", "12345678", "Initial name");

        mockMvc.perform(put("/api/v1/customers/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerJson("RUC", "20123456789", "Updated name")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentType").value("RUC"))
                .andExpect(jsonPath("$.documentNumber").value("20123456789"))
                .andExpect(jsonPath("$.name").value("Updated name"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void changesStatusIdempotentlyWithoutDeletingTheCustomer() throws Exception {
        long id = createCustomer("DNI", "12345678", "Status customer");

        MvcResult firstChange = mockMvc.perform(patch("/api/v1/customers/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false))
                .andReturn();
        String updatedAt = objectMapper.readTree(firstChange.getResponse().getContentAsString())
                .get("updatedAt").asText();

        mockMvc.perform(patch("/api/v1/customers/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updatedAt").value(updatedAt));

        mockMvc.perform(get("/api/v1/customers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void returnsNotFoundForUnknownCustomer() throws Exception {
        mockMvc.perform(get("/api/v1/customers/999"))
                .andExpect(status().isNotFound())
                .andExpect(header().string("X-Correlation-Id", not(blankOrNullString())))
                .andExpect(jsonPath("$.code").value("CUSTOMER_NOT_FOUND"));
    }

    @Test
    void listsCustomersWithDefaultAndExplicitPagination() throws Exception {
        createCustomer("OTHER", "DOC-3", "Zulu");
        createCustomer("OTHER", "DOC-1", "Alpha");
        createCustomer("OTHER", "DOC-2", "Beta");

        mockMvc.perform(get("/api/v1/customers").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Alpha"))
                .andExpect(jsonPath("$.content[1].name").value("Beta"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));

        mockMvc.perform(get("/api/v1/customers")
                        .param("page", "1")
                        .param("size", "2")
                        .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Zulu"));

        mockMvc.perform(get("/api/v1/customers").param("page", "8").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(3));

        mockMvc.perform(get("/api/v1/customers")
                        .param("size", "100")
                        .param("sort", "documentNumber,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].documentNumber").value("DOC-3"))
                .andExpect(jsonPath("$.size").value(100));

        mockMvc.perform(get("/api/v1/customers").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void filtersByStatusAndSearchesNameOrDocumentLiterally() throws Exception {
        long inactiveId = createCustomer("OTHER", "INACTIVE-01", "Dormant");
        createCustomer("RUC", "20123456789", "Comercial Andina");
        createCustomer("OTHER", "PERCENT-01", "100% Real");
        mockMvc.perform(patch("/api/v1/customers/{id}/status", inactiveId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":false}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/customers").param("active", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Dormant"));

        mockMvc.perform(get("/api/v1/customers").param("search", "aNdInA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].documentNumber").value("20123456789"));

        mockMvc.perform(get("/api/v1/customers").param("search", "34567"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Comercial Andina"));

        mockMvc.perform(get("/api/v1/customers").param("search", "%"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("100% Real"));

        mockMvc.perform(get("/api/v1/customers")
                        .param("active", "false")
                        .param("search", "andina"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        mockMvc.perform(get("/api/v1/customers").param("search", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @ParameterizedTest
    @ValueSource(strings = {"id", "name", "documentNumber", "active", "createdAt", "updatedAt"})
    void acceptsEveryDeclaredSortField(String field) throws Exception {
        createCustomer("OTHER", "SORT-01", "Sort customer");

        mockMvc.perform(get("/api/v1/customers").param("sort", field + ",desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void rejectsInvalidPaginationSortAndParameterTypes() throws Exception {
        mockMvc.perform(get("/api/v1/customers").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PAGINATION"));
        mockMvc.perform(get("/api/v1/customers").param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PAGINATION"));
        mockMvc.perform(get("/api/v1/customers").param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PAGINATION"));
        mockMvc.perform(get("/api/v1/customers").param("sort", "email,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_SORT"));
        mockMvc.perform(get("/api/v1/customers").param("sort", "name,sideways"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_SORT"));
        mockMvc.perform(get("/api/v1/customers").param("active", "sometimes"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        mockMvc.perform(get("/api/v1/customers").param("page", "not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    private long createCustomer(String documentType, String documentNumber, String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerJson(documentType, documentNumber, name)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("id").asLong();
    }

    private String customerJson(String documentType, String documentNumber, String name) {
        return """
                {
                  "documentType": "%s",
                  "documentNumber": "%s",
                  "name": "%s",
                  "email": "contact@example.com",
                  "phone": "+51 999 111 222",
                  "address": "Lima, Peru"
                }
                """.formatted(documentType, documentNumber, name);
    }
}
