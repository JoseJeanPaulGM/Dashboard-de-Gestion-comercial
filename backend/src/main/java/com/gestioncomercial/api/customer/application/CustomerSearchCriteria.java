package com.gestioncomercial.api.customer.application;

import com.gestioncomercial.api.shared.pagination.PageCriteria;

public record CustomerSearchCriteria(
        Boolean active,
        String search,
        PageCriteria pageCriteria
) {
}
