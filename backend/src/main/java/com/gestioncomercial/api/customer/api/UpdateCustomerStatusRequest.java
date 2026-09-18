package com.gestioncomercial.api.customer.api;

import jakarta.validation.constraints.NotNull;

public record UpdateCustomerStatusRequest(
        @NotNull(message = "active must not be null")
        Boolean active
) {
}
