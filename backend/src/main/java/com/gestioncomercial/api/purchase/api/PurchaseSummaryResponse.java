package com.gestioncomercial.api.purchase.api;

import com.gestioncomercial.api.purchase.domain.Purchase;
import com.gestioncomercial.api.purchase.domain.PurchaseStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record PurchaseSummaryResponse(Long id, Long supplierId, PurchaseStatus status, BigDecimal total,
                                      String currency, Instant createdAt, Instant confirmedAt) {
    static PurchaseSummaryResponse from(Purchase purchase) {
        return new PurchaseSummaryResponse(purchase.getId(), purchase.getSupplier().getId(), purchase.getStatus(),
                purchase.getTotal(), "PEN", purchase.getCreatedAt(), purchase.getConfirmedAt());
    }
}
