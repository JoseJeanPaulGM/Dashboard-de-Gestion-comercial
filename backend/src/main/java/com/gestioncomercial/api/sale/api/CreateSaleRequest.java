package com.gestioncomercial.api.sale.api;

import com.gestioncomercial.api.sale.application.CreateSaleCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateSaleRequest(
        @NotNull @Positive Long customerId,
        @NotEmpty @Size(max = 100) List<@NotNull @Valid CreateSaleLineRequest> lines
) {
    CreateSaleCommand toCommand() {
        return new CreateSaleCommand(customerId, lines.stream().map(CreateSaleLineRequest::toCommand).toList());
    }
}
