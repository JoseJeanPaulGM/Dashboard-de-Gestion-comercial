package com.gestioncomercial.api.supplier.infrastructure;

import com.gestioncomercial.api.shared.pagination.PageCriteria;
import com.gestioncomercial.api.shared.pagination.PageResult;
import com.gestioncomercial.api.shared.pagination.SortDirection;
import com.gestioncomercial.api.supplier.application.SupplierRepository;
import com.gestioncomercial.api.supplier.application.SupplierSearchCriteria;
import com.gestioncomercial.api.supplier.domain.Supplier;
import com.gestioncomercial.api.supplier.domain.SupplierRucConflictException;
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
class SupplierPersistenceAdapter implements SupplierRepository {
    private final SpringDataSupplierRepository repository;

    SupplierPersistenceAdapter(SpringDataSupplierRepository repository) { this.repository = repository; }

    @Override
    public Supplier save(Supplier supplier) {
        try { return repository.saveAndFlush(supplier); }
        catch (DataIntegrityViolationException exception) { throw new SupplierRucConflictException(); }
    }

    @Override public Optional<Supplier> findById(long id) { return repository.findById(id); }
    @Override public boolean existsByRuc(String ruc) { return repository.existsByRuc(ruc); }
    @Override public boolean existsByRucExcludingId(String ruc, long id) { return repository.existsByRucAndIdNot(ruc, id); }

    @Override
    public PageResult<Supplier> findAll(SupplierSearchCriteria criteria) {
        PageCriteria pc = criteria.pageCriteria();
        Page<Supplier> page = repository.findAll(specification(criteria), PageRequest.of(pc.page(), pc.size(), sort(pc)));
        return new PageResult<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    private Specification<Supplier> specification(SupplierSearchCriteria criteria) {
        Specification<Supplier> specification = (root, query, builder) -> builder.conjunction();
        if (criteria.active() != null) specification = specification.and((root, query, builder) ->
                builder.equal(root.get("active"), criteria.active()));
        if (criteria.search() != null) {
            String pattern = "%" + escapeLike(criteria.search().toLowerCase(Locale.ROOT)) + "%";
            specification = specification.and((root, query, builder) -> {
                Expression<String> ruc = builder.lower(root.get("ruc"));
                Expression<String> businessName = builder.lower(root.get("businessName"));
                Expression<String> tradeName = builder.lower(root.get("tradeName"));
                return builder.or(builder.like(ruc, pattern, '\\'), builder.like(businessName, pattern, '\\'),
                        builder.like(tradeName, pattern, '\\'));
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
