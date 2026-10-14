package com.gestioncomercial.api.purchase.application;

import com.gestioncomercial.api.purchase.domain.PurchaseStatus;
import com.gestioncomercial.api.shared.pagination.PageCriteria;

public record PurchaseSearchCriteria(PurchaseStatus status, Long supplierId, PageCriteria pageCriteria) {
}
