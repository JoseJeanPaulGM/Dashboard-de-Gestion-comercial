package com.gestioncomercial.api.supplier.application;

import com.gestioncomercial.api.shared.pagination.PageCriteria;

public record SupplierProductSearchCriteria(long supplierId, Boolean active, String search,
                                            PageCriteria pageCriteria) {
}
