package com.gestioncomercial.api.supplier.infrastructure;

import com.gestioncomercial.api.supplier.domain.SupplierProduct;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface SpringDataSupplierProductRepository
        extends JpaRepository<SupplierProduct, Long>, JpaSpecificationExecutor<SupplierProduct> {
    boolean existsBySupplierIdAndProductId(long supplierId, long productId);
    boolean existsBySupplierIdAndActiveTrue(long supplierId);
    boolean existsByProductIdAndActiveTrue(long productId);

    @EntityGraph(attributePaths = {"supplier", "product", "product.category"})
    Optional<SupplierProduct> findBySupplierIdAndProductId(long supplierId, long productId);

    @Override
    @EntityGraph(attributePaths = {"supplier", "product", "product.category"})
    Page<SupplierProduct> findAll(Specification<SupplierProduct> specification, Pageable pageable);
}
