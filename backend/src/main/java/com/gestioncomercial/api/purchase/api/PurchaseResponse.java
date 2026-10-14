package com.gestioncomercial.api.purchase.api;

import com.gestioncomercial.api.purchase.domain.Purchase;
import com.gestioncomercial.api.purchase.domain.PurchaseStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PurchaseResponse(Long id, Long supplierId, PurchaseStatus status, List<PurchaseLineResponse> lines,
                               BigDecimal total, String currency, Instant createdAt, Instant confirmedAt) {
    static PurchaseResponse from(Purchase purchase) {
        return new PurchaseResponse(purchase.getId(), purchase.getSupplier().getId(), purchase.getStatus(),
                purchase.getLines().stream().map(PurchaseLineResponse::from).toList(), purchase.getTotal(),
                "PEN", purchase.getCreatedAt(), purchase.getConfirmedAt());
    }
}
