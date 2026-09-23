package com.gestioncomercial.api.supplier.api;

import jakarta.validation.constraints.NotNull;

public record UpdateSupplierStatusRequest(@NotNull(message = "active must not be null") Boolean active) {
}
