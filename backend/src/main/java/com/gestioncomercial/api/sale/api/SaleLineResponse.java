package com.gestioncomercial.api.sale.api;

import com.gestioncomercial.api.sale.domain.SaleLine;
import java.math.BigDecimal;

public record SaleLineResponse(Long id, Long productId, int quantity,
                                   BigDecimal unitPrice, BigDecimal subtotal) {
    static SaleLineResponse from(SaleLine line) {
        return new SaleLineResponse(line.getId(), line.getProduct().getId(), line.getQuantity(),
                line.getUnitPrice(), line.getSubtotal());
    }
}
