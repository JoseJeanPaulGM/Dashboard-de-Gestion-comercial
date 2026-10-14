package com.gestioncomercial.api.sale.api;

import com.gestioncomercial.api.sale.domain.Sale;
import com.gestioncomercial.api.sale.domain.SaleStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record SaleSummaryResponse(Long id, Long customerId, SaleStatus status, BigDecimal total,
                                      String currency, Instant createdAt, Instant confirmedAt) {
    static SaleSummaryResponse from(Sale sale) {
        return new SaleSummaryResponse(sale.getId(), sale.getCustomer().getId(), sale.getStatus(),
                sale.getTotal(), "PEN", sale.getCreatedAt(), sale.getConfirmedAt());
    }
}
