package com.gestioncomercial.api.system;

import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.gestioncomercial.api.support.postgresql.PostgreSqlTestConfiguration;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(PostgreSqlTestConfiguration.class)
class SystemApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsApplicationInformationAndGeneratesCorrelationId() throws Exception {
        mockMvc.perform(get("/api/v1/system/info"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Correlation-Id", not(blankOrNullString())))
                .andExpect(jsonPath("$.name").value("Gestión Comercial API"))
                .andExpect(jsonPath("$.version").value("0.1.0-test"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void echoesAValidRequestAndPreservesCorrelationId() throws Exception {
        mockMvc.perform(post("/api/v1/system/echo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Correlation-Id", "v2-test-request")
                        .content("{\"message\":\"backend base\"}"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Correlation-Id", "v2-test-request"))
                .andExpect(jsonPath("$.message").value("backend base"));
    }

    @Test
    void returnsProblemDetailsForAnInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/v1/system/echo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://gestion-comercial.dev/problems/validation-error"))
                .andExpect(jsonPath("$.title").value("Request validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("One or more request fields are invalid."))
                .andExpect(jsonPath("$.instance").value("/api/v1/system/echo"))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/api/v1/system/echo"))
                .andExpect(jsonPath("$.correlationId").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].field").value("message"));
    }

    @Test
    void exposesHealthAndOpenApiDocumentation() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").isNotEmpty())
                .andExpect(jsonPath("$.info.title").value("Gestión Comercial API"));

        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
    }
}
