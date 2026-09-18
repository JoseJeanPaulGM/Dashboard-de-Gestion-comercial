package com.gestioncomercial.api.catalog.api;

import com.gestioncomercial.api.catalog.domain.Category;
import java.time.Instant;

public record CategoryResponse(
        Long id,
        String code,
        String name,
        String description,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {

    static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getCode(),
                category.getName(),
                category.getDescription(),
                category.isActive(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}
