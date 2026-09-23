package com.gestioncomercial.api.supplier.application;

import com.gestioncomercial.api.shared.pagination.PageResult;
import com.gestioncomercial.api.supplier.domain.Supplier;
import java.util.Optional;

public interface SupplierRepository {
    Supplier save(Supplier supplier);
    Optional<Supplier> findById(long id);
    boolean existsByRuc(String ruc);
    boolean existsByRucExcludingId(String ruc, long id);
    PageResult<Supplier> findAll(SupplierSearchCriteria criteria);
}
