package com.gestioncomercial.api.purchase.api;

import com.gestioncomercial.api.purchase.application.CreatePurchaseCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreatePurchaseRequest(
        @NotNull @Positive Long supplierId,
        @NotEmpty @Size(max = 100) List<@NotNull @Valid CreatePurchaseLineRequest> lines
) {
    CreatePurchaseCommand toCommand() {
        return new CreatePurchaseCommand(supplierId, lines.stream().map(CreatePurchaseLineRequest::toCommand).toList());
    }
}
