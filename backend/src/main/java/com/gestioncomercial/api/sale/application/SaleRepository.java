package com.gestioncomercial.api.sale.application;

import com.gestioncomercial.api.sale.domain.Sale;
import com.gestioncomercial.api.shared.pagination.PageResult;
import java.util.Optional;

public interface SaleRepository {
    Sale save(Sale sale);
    Optional<Sale> findById(long id);
    Optional<Sale> findByIdForUpdate(long id);
    PageResult<Sale> findAll(SaleSearchCriteria criteria);
}
