package com.gestioncomercial.api.sale.application;

import java.util.List;

public record CreateSaleCommand(long customerId, List<Line> lines) {
    public record Line(long productId, int quantity) {
    }
}
