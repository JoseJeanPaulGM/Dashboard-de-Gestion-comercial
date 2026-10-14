package com.gestioncomercial.api.sale.api;

import com.gestioncomercial.api.sale.domain.Sale;
import com.gestioncomercial.api.sale.domain.SaleStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record SaleResponse(Long id, Long customerId, SaleStatus status, List<SaleLineResponse> lines,
                               BigDecimal total, String currency, Instant createdAt, Instant confirmedAt) {
    static SaleResponse from(Sale sale) {
        return new SaleResponse(sale.getId(), sale.getCustomer().getId(), sale.getStatus(),
                sale.getLines().stream().map(SaleLineResponse::from).toList(), sale.getTotal(),
                "PEN", sale.getCreatedAt(), sale.getConfirmedAt());
    }
}
