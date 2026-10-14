package com.gestioncomercial.api.purchase.domain;

import com.gestioncomercial.api.catalog.domain.Product;
import java.math.BigDecimal;

public record PurchaseLineInput(Product product, int quantity, BigDecimal unitPrice) {
}
