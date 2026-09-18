package com.gestioncomercial.api.catalog.application;

import java.math.BigDecimal;

public record CreateProductCommand(
        String sku,
        String name,
        String description,
        BigDecimal salePrice,
        long categoryId
) {
}
