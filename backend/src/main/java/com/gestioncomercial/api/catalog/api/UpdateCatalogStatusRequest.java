package com.gestioncomercial.api.catalog.api;

import jakarta.validation.constraints.NotNull;

public record UpdateCatalogStatusRequest(
        @NotNull(message = "active must not be null")
        Boolean active
) {
}
