package com.gestioncomercial.api.catalog.application;

import com.gestioncomercial.api.catalog.domain.Product;
import com.gestioncomercial.api.shared.pagination.PageResult;
import java.util.Optional;

public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(long id);

    boolean existsBySku(String sku);

    boolean existsBySkuExcludingId(String sku, long id);

    boolean existsActiveByCategoryId(long categoryId);

    PageResult<Product> findAll(ProductSearchCriteria criteria);
}
