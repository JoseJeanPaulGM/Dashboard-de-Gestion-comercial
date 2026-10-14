package com.gestioncomercial.api.inventory.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record AdjustmentRequest(
        @NotNull @Positive Long productId,
        @NotNull Long quantityChange,
        @NotBlank @Size(min = 10, max = 500) String reason,
        @NotNull UUID requestKey
) {
}
