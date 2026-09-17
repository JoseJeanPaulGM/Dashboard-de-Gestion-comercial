package com.gestioncomercial.api.shared.api;

import com.gestioncomercial.api.shared.observability.CorrelationIdFilter;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ProblemDetail> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        List<FieldViolation> violations = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldViolation(error.getField(), Objects.requireNonNullElse(error.getDefaultMessage(), "Invalid value")))
                .collect(Collectors.toList());

        ProblemDetail problemDetail = problem(
                HttpStatus.BAD_REQUEST,
                "Request validation failed",
                "One or more request fields are invalid.",
                "VALIDATION_ERROR",
                request
        );
        problemDetail.setProperty("errors", violations);
        return ResponseEntity.badRequest().body(problemDetail);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> handleUnexpected(Exception exception, HttpServletRequest request) {
        String correlationId = correlationId(request);
        LOGGER.error("Unhandled request failure; correlationId={}", correlationId, exception);

        ProblemDetail problemDetail = problem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected server error",
                "The server could not process the request.",
                "INTERNAL_ERROR",
                request
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }

    private ProblemDetail problem(
            HttpStatus status,
            String title,
            String detail,
            String code,
            HttpServletRequest request
    ) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setType(URI.create("https://gestion-comercial.dev/problems/" + code.toLowerCase().replace('_', '-')));
        problemDetail.setTitle(title);
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("code", code);
        problemDetail.setProperty("timestamp", Instant.now().toString());
        problemDetail.setProperty("path", request.getRequestURI());
        problemDetail.setProperty("correlationId", correlationId(request));
        return problemDetail;
    }

    private String correlationId(HttpServletRequest request) {
        Object value = request.getAttribute(CorrelationIdFilter.REQUEST_ATTRIBUTE);
        return value == null ? "unknown" : value.toString();
    }
}
