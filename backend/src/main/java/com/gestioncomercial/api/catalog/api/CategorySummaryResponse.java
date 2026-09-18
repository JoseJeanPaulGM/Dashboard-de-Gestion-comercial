package com.gestioncomercial.api.catalog.api;

import com.gestioncomercial.api.catalog.domain.Category;

public record CategorySummaryResponse(Long id, String code, String name, boolean active) {

    static CategorySummaryResponse from(Category category) {
        return new CategorySummaryResponse(
                category.getId(),
                category.getCode(),
                category.getName(),
                category.isActive()
        );
    }
}
