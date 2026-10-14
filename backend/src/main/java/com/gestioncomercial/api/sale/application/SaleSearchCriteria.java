package com.gestioncomercial.api.sale.application;

import com.gestioncomercial.api.sale.domain.SaleStatus;
import com.gestioncomercial.api.shared.pagination.PageCriteria;

public record SaleSearchCriteria(SaleStatus status, Long customerId, PageCriteria pageCriteria) {
}
