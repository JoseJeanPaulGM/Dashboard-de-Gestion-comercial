package com.gestioncomercial.api.catalog.infrastructure;

import com.gestioncomercial.api.catalog.application.ProductRepository;
import com.gestioncomercial.api.catalog.application.ProductSearchCriteria;
import com.gestioncomercial.api.catalog.domain.Product;
import com.gestioncomercial.api.catalog.domain.ProductSkuConflictException;
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
class ProductPersistenceAdapter implements ProductRepository {

    private final SpringDataProductRepository repository;

    ProductPersistenceAdapter(SpringDataProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public Product save(Product product) {
        try {
            return repository.saveAndFlush(product);
        } catch (DataIntegrityViolationException exception) {
            throw new ProductSkuConflictException();
        }
    }

    @Override
    public Optional<Product> findById(long id) {
        return repository.findWithCategoryById(id);
    }

    @Override
    public boolean existsBySku(String sku) {
        return repository.existsBySku(sku);
    }

    @Override
    public boolean existsBySkuExcludingId(String sku, long id) {
        return repository.existsBySkuAndIdNot(sku, id);
    }

    @Override
    public boolean existsActiveByCategoryId(long categoryId) {
        return repository.existsByCategoryIdAndActiveTrue(categoryId);
    }

    @Override
    public PageResult<Product> findAll(ProductSearchCriteria criteria) {
        PageCriteria pageCriteria = criteria.pageCriteria();
        PageRequest pageRequest = PageRequest.of(
                pageCriteria.page(),
                pageCriteria.size(),
                sort(pageCriteria)
        );
        Page<Product> page = repository.findAll(specification(criteria), pageRequest);
        return new PageResult<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    private Specification<Product> specification(ProductSearchCriteria criteria) {
        Specification<Product> specification = (root, query, builder) -> builder.conjunction();

        if (criteria.active() != null) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("active"), criteria.active()));
        }
        if (criteria.categoryId() != null) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("category").get("id"), criteria.categoryId()));
        }
        if (criteria.minPrice() != null) {
            specification = specification.and((root, query, builder) ->
                    builder.greaterThanOrEqualTo(root.get("salePrice"), criteria.minPrice()));
        }
        if (criteria.maxPrice() != null) {
            specification = specification.and((root, query, builder) ->
                    builder.lessThanOrEqualTo(root.get("salePrice"), criteria.maxPrice()));
        }
        if (criteria.search() != null) {
            String pattern = "%" + escapeLike(criteria.search().toLowerCase(Locale.ROOT)) + "%";
            specification = specification.and((root, query, builder) -> {
                Expression<String> lowerSku = builder.lower(root.get("sku"));
                Expression<String> lowerName = builder.lower(root.get("name"));
                return builder.or(
                        builder.like(lowerSku, pattern, '\\'),
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
