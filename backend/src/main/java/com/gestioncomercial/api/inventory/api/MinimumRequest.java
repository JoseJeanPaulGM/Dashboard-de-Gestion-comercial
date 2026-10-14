package com.gestioncomercial.api.inventory.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record MinimumRequest(@NotNull @PositiveOrZero Long minimumQuantity) {
}
