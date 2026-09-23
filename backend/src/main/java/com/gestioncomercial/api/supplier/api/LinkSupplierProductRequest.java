package com.gestioncomercial.api.supplier.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record LinkSupplierProductRequest(
        @NotNull(message = "productId must not be null")
        @Positive(message = "productId must be greater than zero") Long productId,
        @Size(max = 80, message = "supplierProductCode must not exceed 80 characters") String supplierProductCode
) {
}
