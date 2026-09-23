package com.gestioncomercial.api.supplier.api;

import jakarta.validation.constraints.Size;

public record UpdateSupplierProductRequest(
        @Size(max = 80, message = "supplierProductCode must not exceed 80 characters") String supplierProductCode
) {
}
