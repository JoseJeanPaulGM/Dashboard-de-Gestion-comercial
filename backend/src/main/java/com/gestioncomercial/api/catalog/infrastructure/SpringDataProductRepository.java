package com.gestioncomercial.api.catalog.infrastructure;

import com.gestioncomercial.api.catalog.domain.Product;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface SpringDataProductRepository
        extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, long id);

    boolean existsByCategoryIdAndActiveTrue(long categoryId);

    @EntityGraph(attributePaths = "category")
    Optional<Product> findWithCategoryById(long id);

    @Override
    @EntityGraph(attributePaths = "category")
    Page<Product> findAll(Specification<Product> specification, Pageable pageable);
}
