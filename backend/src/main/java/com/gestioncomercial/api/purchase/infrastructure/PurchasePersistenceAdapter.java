package com.gestioncomercial.api.purchase.infrastructure;

import com.gestioncomercial.api.purchase.application.PurchaseRepository;
import com.gestioncomercial.api.purchase.application.PurchaseSearchCriteria;
import com.gestioncomercial.api.purchase.domain.Purchase;
import com.gestioncomercial.api.shared.pagination.PageCriteria;
import com.gestioncomercial.api.shared.pagination.PageResult;
import com.gestioncomercial.api.shared.pagination.SortDirection;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
class PurchasePersistenceAdapter implements PurchaseRepository {
    private final SpringDataPurchaseRepository repository;

    PurchasePersistenceAdapter(SpringDataPurchaseRepository repository) {
        this.repository = repository;
    }

    @Override
    public Purchase save(Purchase purchase) {
        return repository.saveAndFlush(purchase);
    }

    @Override
    public Optional<Purchase> findById(long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<Purchase> findByIdForUpdate(long id) {
        return repository.findByIdForUpdate(id);
    }

    @Override
    public PageResult<Purchase> findAll(PurchaseSearchCriteria criteria) {
        Specification<Purchase> filter = (root, query, builder) -> builder.conjunction();
        if (criteria.status() != null) {
            filter = filter.and((root, query, builder) -> builder.equal(root.get("status"), criteria.status()));
        }
        if (criteria.supplierId() != null) {
            filter = filter.and((root, query, builder) ->
                    builder.equal(root.get("supplier").get("id"), criteria.supplierId()));
        }
        PageCriteria pageCriteria = criteria.pageCriteria();
        Sort.Direction direction = pageCriteria.direction() == SortDirection.ASC
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, pageCriteria.sortProperty());
        if (!"id".equals(pageCriteria.sortProperty())) sort = sort.and(Sort.by("id"));
        Page<Purchase> page = repository.findAll(filter,
                PageRequest.of(pageCriteria.page(), pageCriteria.size(), sort));
        return new PageResult<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
