package com.gestioncomercial.api.catalog.application;

import java.math.BigDecimal;

public record UpdateProductCommand(
        String sku,
        String name,
        String description,
        BigDecimal salePrice,
        long categoryId
) {
}
