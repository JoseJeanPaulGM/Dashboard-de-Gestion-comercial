package com.gestioncomercial.api.supplier.application;

import com.gestioncomercial.api.shared.pagination.PageResult;
import com.gestioncomercial.api.supplier.domain.SupplierProduct;
import java.util.Optional;

public interface SupplierProductRepository {
    SupplierProduct save(SupplierProduct link);
    Optional<SupplierProduct> findBySupplierIdAndProductId(long supplierId, long productId);
    boolean existsBySupplierIdAndProductId(long supplierId, long productId);
    boolean existsActiveBySupplierId(long supplierId);
    boolean existsActiveByProductId(long productId);
    PageResult<SupplierProduct> findAll(SupplierProductSearchCriteria criteria);
}
