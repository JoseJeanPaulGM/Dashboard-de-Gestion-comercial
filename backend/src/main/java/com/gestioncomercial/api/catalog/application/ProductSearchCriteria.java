package com.gestioncomercial.api.catalog.application;

import com.gestioncomercial.api.shared.pagination.PageCriteria;
import java.math.BigDecimal;

public record ProductSearchCriteria(
        Boolean active,
        Long categoryId,
        String search,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        PageCriteria pageCriteria
) {
}
