package com.gestioncomercial.api.shared.api;

import com.gestioncomercial.api.catalog.domain.CategoryCodeConflictException;
import com.gestioncomercial.api.catalog.domain.CategoryHasActiveProductsException;
import com.gestioncomercial.api.catalog.domain.CategoryNotFoundException;
import com.gestioncomercial.api.catalog.domain.ProductCategoryInactiveException;
import com.gestioncomercial.api.catalog.domain.ProductNotFoundException;
import com.gestioncomercial.api.catalog.domain.ProductSkuConflictException;
import com.gestioncomercial.api.customer.domain.CustomerDocumentConflictException;
import com.gestioncomercial.api.customer.domain.CustomerNotFoundException;
import com.gestioncomercial.api.shared.observability.CorrelationIdFilter;
import com.gestioncomercial.api.shared.pagination.InvalidPaginationException;
import com.gestioncomercial.api.shared.pagination.InvalidSortException;
import java.net.URI;
import java.time.Clock;
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
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final Clock clock;

    public GlobalExceptionHandler(Clock clock) {
        this.clock = clock;
    }

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

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    ResponseEntity<ProblemDetail> handleInvalidRequest(Exception exception, HttpServletRequest request) {
        return response(
                HttpStatus.BAD_REQUEST,
                "Invalid request",
                "The request contains a malformed or unsupported value.",
                "INVALID_REQUEST",
                request
        );
    }

    @ExceptionHandler(InvalidRequestException.class)
    ResponseEntity<ProblemDetail> handleInvalidRequest(InvalidRequestException exception, HttpServletRequest request) {
        return response(
                HttpStatus.BAD_REQUEST,
                "Invalid request",
                exception.getMessage(),
                "INVALID_REQUEST",
                request
        );
    }

    @ExceptionHandler(InvalidPaginationException.class)
    ResponseEntity<ProblemDetail> handleInvalidPagination(
            InvalidPaginationException exception,
            HttpServletRequest request
    ) {
        return response(
                HttpStatus.BAD_REQUEST,
                "Invalid pagination",
                exception.getMessage(),
                "INVALID_PAGINATION",
                request
        );
    }

    @ExceptionHandler(InvalidSortException.class)
    ResponseEntity<ProblemDetail> handleInvalidSort(InvalidSortException exception, HttpServletRequest request) {
        return response(
                HttpStatus.BAD_REQUEST,
                "Invalid sort",
                exception.getMessage(),
                "INVALID_SORT",
                request
        );
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    ResponseEntity<ProblemDetail> handleCustomerNotFound(
            CustomerNotFoundException exception,
            HttpServletRequest request
    ) {
        return response(
                HttpStatus.NOT_FOUND,
                "Customer not found",
                exception.getMessage(),
                "CUSTOMER_NOT_FOUND",
                request
        );
    }

    @ExceptionHandler(CustomerDocumentConflictException.class)
    ResponseEntity<ProblemDetail> handleCustomerDocumentConflict(
            CustomerDocumentConflictException exception,
            HttpServletRequest request
    ) {
        return response(
                HttpStatus.CONFLICT,
                "Customer document conflict",
                exception.getMessage(),
                "CUSTOMER_DOCUMENT_CONFLICT",
                request
        );
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    ResponseEntity<ProblemDetail> handleCategoryNotFound(
            CategoryNotFoundException exception,
            HttpServletRequest request
    ) {
        return response(HttpStatus.NOT_FOUND, "Category not found", exception.getMessage(), "CATEGORY_NOT_FOUND", request);
    }

    @ExceptionHandler(CategoryCodeConflictException.class)
    ResponseEntity<ProblemDetail> handleCategoryCodeConflict(
            CategoryCodeConflictException exception,
            HttpServletRequest request
    ) {
        return response(HttpStatus.CONFLICT, "Category code conflict", exception.getMessage(), "CATEGORY_CODE_CONFLICT", request);
    }

    @ExceptionHandler(CategoryHasActiveProductsException.class)
    ResponseEntity<ProblemDetail> handleCategoryHasActiveProducts(
            CategoryHasActiveProductsException exception,
            HttpServletRequest request
    ) {
        return response(
                HttpStatus.CONFLICT,
                "Category has active products",
                exception.getMessage(),
                "CATEGORY_HAS_ACTIVE_PRODUCTS",
                request
        );
    }

    @ExceptionHandler(ProductNotFoundException.class)
    ResponseEntity<ProblemDetail> handleProductNotFound(
            ProductNotFoundException exception,
            HttpServletRequest request
    ) {
        return response(HttpStatus.NOT_FOUND, "Product not found", exception.getMessage(), "PRODUCT_NOT_FOUND", request);
    }

    @ExceptionHandler(ProductSkuConflictException.class)
    ResponseEntity<ProblemDetail> handleProductSkuConflict(
            ProductSkuConflictException exception,
            HttpServletRequest request
    ) {
        return response(HttpStatus.CONFLICT, "Product SKU conflict", exception.getMessage(), "PRODUCT_SKU_CONFLICT", request);
    }

    @ExceptionHandler(ProductCategoryInactiveException.class)
    ResponseEntity<ProblemDetail> handleProductCategoryInactive(
            ProductCategoryInactiveException exception,
            HttpServletRequest request
    ) {
        return response(
                HttpStatus.CONFLICT,
                "Product category inactive",
                exception.getMessage(),
                "PRODUCT_CATEGORY_INACTIVE",
                request
        );
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
        problemDetail.setProperty("timestamp", Instant.now(clock).toString());
        problemDetail.setProperty("path", request.getRequestURI());
        problemDetail.setProperty("correlationId", correlationId(request));
        return problemDetail;
    }

    private ResponseEntity<ProblemDetail> response(
            HttpStatus status,
            String title,
            String detail,
            String code,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(status).body(problem(status, title, detail, code, request));
    }

    private String correlationId(HttpServletRequest request) {
        Object value = request.getAttribute(CorrelationIdFilter.REQUEST_ATTRIBUTE);
        return value == null ? "unknown" : value.toString();
    }
}
