package com.gestioncomercial.api.identity.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestioncomercial.api.shared.observability.CorrelationIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

@Component
public class SecurityProblemWriter {
    private final ObjectMapper mapper;
    private final Clock clock;

    public SecurityProblemWriter(ObjectMapper mapper, Clock clock) {
        this.mapper = mapper;
        this.clock = clock;
    }

    public void write(HttpServletRequest request, HttpServletResponse response,
                      int status, String title, String code, String detail) throws IOException {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(status), detail);
        problem.setType(URI.create("https://gestion-comercial.dev/problems/" + code.toLowerCase().replace('_', '-')));
        problem.setTitle(title);
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", code);
        problem.setProperty("timestamp", Instant.now(clock).toString());
        problem.setProperty("path", request.getRequestURI());
        Object correlation = request.getAttribute(CorrelationIdFilter.REQUEST_ATTRIBUTE);
        problem.setProperty("correlationId", correlation == null ? "unknown" : correlation.toString());
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        mapper.writeValue(response.getOutputStream(), problem);
    }
}
