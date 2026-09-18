package com.gestioncomercial.api.catalog.api;

import com.gestioncomercial.api.catalog.application.CreateProductCommand;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank(message = "sku must not be blank")
        @Size(max = 50, message = "sku must not exceed 50 characters")
        String sku,

        @NotBlank(message = "name must not be blank")
        @Size(max = 150, message = "name must not exceed 150 characters")
        String name,

        @Size(max = 500, message = "description must not exceed 500 characters")
        String description,

        @NotNull(message = "salePrice must not be null")
        @DecimalMin(value = "0.01", message = "salePrice must be at least 0.01")
        @Digits(integer = 10, fraction = 2, message = "salePrice must have at most 10 integer and 2 decimal digits")
        BigDecimal salePrice,

        @NotNull(message = "categoryId must not be null")
        @Positive(message = "categoryId must be greater than zero")
        Long categoryId
) {

    CreateProductCommand toCommand() {
        return new CreateProductCommand(sku, name, description, salePrice, categoryId);
    }
}
