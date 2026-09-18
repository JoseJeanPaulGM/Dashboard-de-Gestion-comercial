package com.gestioncomercial.api.catalog.application;

import com.gestioncomercial.api.shared.pagination.PageCriteria;

public record CategorySearchCriteria(Boolean active, String search, PageCriteria pageCriteria) {
}
