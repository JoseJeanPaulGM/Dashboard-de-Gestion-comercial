package com.gestioncomercial.api.shared.pagination;

import java.util.Locale;
import java.util.Map;

public final class Pagination {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    private Pagination() {
    }

    public static PageCriteria parse(
            int page,
            int size,
            String sort,
            Map<String, String> allowedSortProperties
    ) {
        if (page < 0) {
            throw new InvalidPaginationException("page must be greater than or equal to 0");
        }
        if (size < 1 || size > MAX_SIZE) {
            throw new InvalidPaginationException("size must be between 1 and " + MAX_SIZE);
        }

        String[] parts = sort.split(",", -1);
        if (parts.length != 2) {
            throw new InvalidSortException("sort must use the format field,direction");
        }

        String publicField = parts[0].trim();
        String property = allowedSortProperties.get(publicField);
        if (property == null) {
            throw new InvalidSortException("sort field is not allowed: " + publicField);
        }

        SortDirection direction;
        try {
            direction = SortDirection.valueOf(parts[1].trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new InvalidSortException("sort direction must be asc or desc");
        }

        return new PageCriteria(page, size, property, direction);
    }
}
