package com.gestioncomercial.api.shared.pagination;

public record PageCriteria(
        int page,
        int size,
        String sortProperty,
        SortDirection direction
) {
}
