package com.gestioncomercial.api.purchase.api;

import com.gestioncomercial.api.purchase.application.CreatePurchaseCommand;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CreatePurchaseLineRequest(
        @NotNull @Positive Long productId,
        @NotNull @Positive @Max(1_000_000) Integer quantity,
        @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal unitPrice
) {
    CreatePurchaseCommand.Line toCommand() {
        return new CreatePurchaseCommand.Line(productId, quantity, unitPrice);
    }
}
