package com.gestioncomercial.api.catalog.application;

import com.gestioncomercial.api.catalog.domain.Category;
import com.gestioncomercial.api.shared.pagination.PageResult;
import java.util.Optional;

public interface CategoryRepository {

    Category save(Category category);

    Optional<Category> findById(long id);

    boolean existsByCode(String code);

    boolean existsByCodeExcludingId(String code, long id);

    PageResult<Category> findAll(CategorySearchCriteria criteria);
}
