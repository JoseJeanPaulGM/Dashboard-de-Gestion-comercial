package com.gestioncomercial.api.purchase.application;

import com.gestioncomercial.api.purchase.domain.Purchase;
import com.gestioncomercial.api.shared.pagination.PageResult;
import java.util.Optional;

public interface PurchaseRepository {
    Purchase save(Purchase purchase);
    Optional<Purchase> findById(long id);
    Optional<Purchase> findByIdForUpdate(long id);
    PageResult<Purchase> findAll(PurchaseSearchCriteria criteria);
}
