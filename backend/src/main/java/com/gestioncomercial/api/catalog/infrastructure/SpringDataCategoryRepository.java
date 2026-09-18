package com.gestioncomercial.api.catalog.infrastructure;

import com.gestioncomercial.api.catalog.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface SpringDataCategoryRepository
        extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, long id);
}
