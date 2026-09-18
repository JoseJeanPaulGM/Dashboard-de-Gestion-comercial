package com.gestioncomercial.api.catalog.api;

import com.gestioncomercial.api.catalog.domain.Product;
import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        String description,
        BigDecimal salePrice,
        String currency,
        CategorySummaryResponse category,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {

    static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getSalePrice(),
                "PEN",
                CategorySummaryResponse.from(product.getCategory()),
                product.isActive(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
