package com.gestioncomercial.api.sale.api;

import com.gestioncomercial.api.sale.application.CreateSaleCommand;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
public record CreateSaleLineRequest(
        @NotNull @Positive Long productId,
        @NotNull @Positive @Max(1_000_000) Integer quantity
) {
    CreateSaleCommand.Line toCommand() {
        return new CreateSaleCommand.Line(productId, quantity);
    }
}
