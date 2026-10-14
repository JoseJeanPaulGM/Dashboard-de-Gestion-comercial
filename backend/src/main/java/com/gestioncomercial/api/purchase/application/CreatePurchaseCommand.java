package com.gestioncomercial.api.purchase.application;

import java.math.BigDecimal;
import java.util.List;

public record CreatePurchaseCommand(long supplierId, List<Line> lines) {
    public record Line(long productId, int quantity, BigDecimal unitPrice) {
    }
}
