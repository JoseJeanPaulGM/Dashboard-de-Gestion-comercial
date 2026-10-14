package com.gestioncomercial.api.purchase.api;

import com.gestioncomercial.api.purchase.domain.PurchaseLine;
import java.math.BigDecimal;

public record PurchaseLineResponse(Long id, Long productId, int quantity,
                                   BigDecimal unitPrice, BigDecimal subtotal) {
    static PurchaseLineResponse from(PurchaseLine line) {
        return new PurchaseLineResponse(line.getId(), line.getProduct().getId(), line.getQuantity(),
                line.getUnitPrice(), line.getSubtotal());
    }
}
