package com.gestioncomercial.api.sale.infrastructure;

import com.gestioncomercial.api.sale.application.SaleRepository;
import com.gestioncomercial.api.sale.application.SaleSearchCriteria;
import com.gestioncomercial.api.sale.domain.Sale;
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
class SalePersistenceAdapter implements SaleRepository {
    private final SpringDataSaleRepository repository;

    SalePersistenceAdapter(SpringDataSaleRepository repository) {
        this.repository = repository;
    }

    @Override
    public Sale save(Sale sale) {
        return repository.saveAndFlush(sale);
    }

    @Override
    public Optional<Sale> findById(long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<Sale> findByIdForUpdate(long id) {
        return repository.findByIdForUpdate(id);
    }

    @Override
    public PageResult<Sale> findAll(SaleSearchCriteria criteria) {
        Specification<Sale> filter = (root, query, builder) -> builder.conjunction();
        if (criteria.status() != null) {
            filter = filter.and((root, query, builder) -> builder.equal(root.get("status"), criteria.status()));
        }
        if (criteria.customerId() != null) {
            filter = filter.and((root, query, builder) ->
                    builder.equal(root.get("customer").get("id"), criteria.customerId()));
        }
        PageCriteria pageCriteria = criteria.pageCriteria();
        Sort.Direction direction = pageCriteria.direction() == SortDirection.ASC
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, pageCriteria.sortProperty());
        if (!"id".equals(pageCriteria.sortProperty())) sort = sort.and(Sort.by("id"));
        Page<Sale> page = repository.findAll(filter,
                PageRequest.of(pageCriteria.page(), pageCriteria.size(), sort));
        return new PageResult<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
