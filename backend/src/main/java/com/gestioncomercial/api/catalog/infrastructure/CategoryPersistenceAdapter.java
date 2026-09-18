package com.gestioncomercial.api.catalog.infrastructure;

import com.gestioncomercial.api.catalog.application.CategoryRepository;
import com.gestioncomercial.api.catalog.application.CategorySearchCriteria;
import com.gestioncomercial.api.catalog.domain.Category;
import com.gestioncomercial.api.catalog.domain.CategoryCodeConflictException;
import com.gestioncomercial.api.shared.pagination.PageCriteria;
import com.gestioncomercial.api.shared.pagination.PageResult;
import com.gestioncomercial.api.shared.pagination.SortDirection;
import jakarta.persistence.criteria.Expression;
import java.util.Locale;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
class CategoryPersistenceAdapter implements CategoryRepository {

    private final SpringDataCategoryRepository repository;

    CategoryPersistenceAdapter(SpringDataCategoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public Category save(Category category) {
        try {
            return repository.saveAndFlush(category);
        } catch (DataIntegrityViolationException exception) {
            throw new CategoryCodeConflictException();
        }
    }

    @Override
    public Optional<Category> findById(long id) {
        return repository.findById(id);
    }

    @Override
    public boolean existsByCode(String code) {
        return repository.existsByCode(code);
    }

    @Override
    public boolean existsByCodeExcludingId(String code, long id) {
        return repository.existsByCodeAndIdNot(code, id);
    }

    @Override
    public PageResult<Category> findAll(CategorySearchCriteria criteria) {
        PageCriteria pageCriteria = criteria.pageCriteria();
        Sort sort = sort(pageCriteria);
        PageRequest pageRequest = PageRequest.of(pageCriteria.page(), pageCriteria.size(), sort);
        Page<Category> page = repository.findAll(specification(criteria), pageRequest);
        return new PageResult<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    private Specification<Category> specification(CategorySearchCriteria criteria) {
        Specification<Category> specification = (root, query, builder) -> builder.conjunction();

        if (criteria.active() != null) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("active"), criteria.active()));
        }

        if (criteria.search() != null) {
            String pattern = "%" + escapeLike(criteria.search().toLowerCase(Locale.ROOT)) + "%";
            specification = specification.and((root, query, builder) -> {
                Expression<String> lowerCode = builder.lower(root.get("code"));
                Expression<String> lowerName = builder.lower(root.get("name"));
                return builder.or(
                        builder.like(lowerCode, pattern, '\\'),
                        builder.like(lowerName, pattern, '\\')
                );
            });
        }

        return specification;
    }

    private Sort sort(PageCriteria criteria) {
        Sort.Direction direction = criteria.direction() == SortDirection.ASC
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, criteria.sortProperty());
        return "id".equals(criteria.sortProperty())
                ? sort
                : sort.and(Sort.by(Sort.Direction.ASC, "id"));
    }

    private String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
