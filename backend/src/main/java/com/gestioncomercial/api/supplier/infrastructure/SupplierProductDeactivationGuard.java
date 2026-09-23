package com.gestioncomercial.api.supplier.infrastructure;

import com.gestioncomercial.api.catalog.application.ProductDeactivationGuard;
import com.gestioncomercial.api.catalog.domain.ProductHasActiveSuppliersException;
import com.gestioncomercial.api.supplier.application.SupplierProductRepository;
import org.springframework.stereotype.Component;

@Component
class SupplierProductDeactivationGuard implements ProductDeactivationGuard {

    private final SupplierProductRepository repository;

    SupplierProductDeactivationGuard(SupplierProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public void ensureCanDeactivate(long productId) {
        if (repository.existsActiveByProductId(productId)) {
            throw new ProductHasActiveSuppliersException();
        }
    }
}
