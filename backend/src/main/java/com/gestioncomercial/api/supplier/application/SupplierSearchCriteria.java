package com.gestioncomercial.api.supplier.application;

import com.gestioncomercial.api.shared.pagination.PageCriteria;

public record SupplierSearchCriteria(Boolean active, String search, PageCriteria pageCriteria) {
}
