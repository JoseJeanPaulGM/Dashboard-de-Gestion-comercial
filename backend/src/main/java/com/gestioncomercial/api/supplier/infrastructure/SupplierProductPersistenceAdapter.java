package com.gestioncomercial.api.supplier.infrastructure;

import com.gestioncomercial.api.shared.pagination.PageCriteria;
import com.gestioncomercial.api.shared.pagination.PageResult;
import com.gestioncomercial.api.shared.pagination.SortDirection;
import com.gestioncomercial.api.supplier.application.SupplierProductRepository;
import com.gestioncomercial.api.supplier.application.SupplierProductSearchCriteria;
import com.gestioncomercial.api.supplier.domain.SupplierProduct;
import com.gestioncomercial.api.supplier.domain.SupplierProductConflictException;
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
class SupplierProductPersistenceAdapter implements SupplierProductRepository {
    private final SpringDataSupplierProductRepository repository;

    SupplierProductPersistenceAdapter(SpringDataSupplierProductRepository repository) { this.repository = repository; }

    @Override
    public SupplierProduct save(SupplierProduct link) {
        try { return repository.saveAndFlush(link); }
        catch (DataIntegrityViolationException exception) { throw new SupplierProductConflictException(); }
    }

    @Override public Optional<SupplierProduct> findBySupplierIdAndProductId(long supplierId, long productId) {
        return repository.findBySupplierIdAndProductId(supplierId, productId);
    }
    @Override public boolean existsBySupplierIdAndProductId(long supplierId, long productId) {
        return repository.existsBySupplierIdAndProductId(supplierId, productId);
    }
    @Override public boolean existsActiveBySupplierId(long supplierId) {
        return repository.existsBySupplierIdAndActiveTrue(supplierId);
    }
    @Override public boolean existsActiveByProductId(long productId) {
        return repository.existsByProductIdAndActiveTrue(productId);
    }

    @Override
    public PageResult<SupplierProduct> findAll(SupplierProductSearchCriteria criteria) {
        PageCriteria pc = criteria.pageCriteria();
        Page<SupplierProduct> page = repository.findAll(specification(criteria),
                PageRequest.of(pc.page(), pc.size(), sort(pc)));
        return new PageResult<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    private Specification<SupplierProduct> specification(SupplierProductSearchCriteria criteria) {
        Specification<SupplierProduct> specification = (root, query, builder) ->
                builder.equal(root.get("supplier").get("id"), criteria.supplierId());
        if (criteria.active() != null) specification = specification.and((root, query, builder) ->
                builder.equal(root.get("active"), criteria.active()));
        if (criteria.search() != null) {
            String pattern = "%" + escapeLike(criteria.search().toLowerCase(Locale.ROOT)) + "%";
            specification = specification.and((root, query, builder) -> {
                Expression<String> sku = builder.lower(root.get("product").get("sku"));
                Expression<String> name = builder.lower(root.get("product").get("name"));
                Expression<String> code = builder.lower(root.get("supplierProductCode"));
                return builder.or(builder.like(sku, pattern, '\\'), builder.like(name, pattern, '\\'),
                        builder.like(code, pattern, '\\'));
            });
        }
        return specification;
    }

    private Sort sort(PageCriteria criteria) {
        Sort.Direction direction = criteria.direction() == SortDirection.ASC ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, criteria.sortProperty());
        return "id".equals(criteria.sortProperty()) ? sort : sort.and(Sort.by(Sort.Direction.ASC, "id"));
    }

    private String escapeLike(String value) { return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_"); }
}
