package com.gestioncomercial.api.customer.infrastructure;

import com.gestioncomercial.api.customer.application.CustomerRepository;
import com.gestioncomercial.api.customer.application.CustomerSearchCriteria;
import com.gestioncomercial.api.customer.domain.Customer;
import com.gestioncomercial.api.customer.domain.CustomerDocumentConflictException;
import com.gestioncomercial.api.customer.domain.DocumentType;
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
class CustomerPersistenceAdapter implements CustomerRepository {

    private final SpringDataCustomerRepository repository;

    CustomerPersistenceAdapter(SpringDataCustomerRepository repository) {
        this.repository = repository;
    }

    @Override
    public Customer save(Customer customer) {
        try {
            return repository.saveAndFlush(customer);
        } catch (DataIntegrityViolationException exception) {
            throw new CustomerDocumentConflictException();
        }
    }

    @Override
    public Optional<Customer> findById(long id) {
        return repository.findById(id);
    }

    @Override
    public boolean existsByDocument(DocumentType documentType, String documentNumber) {
        return repository.existsByDocumentTypeAndDocumentNumber(documentType, documentNumber);
    }

    @Override
    public boolean existsByDocumentExcludingId(DocumentType documentType, String documentNumber, long id) {
        return repository.existsByDocumentTypeAndDocumentNumberAndIdNot(documentType, documentNumber, id);
    }

    @Override
    public PageResult<Customer> findAll(CustomerSearchCriteria criteria) {
        PageCriteria pageCriteria = criteria.pageCriteria();
        Sort.Direction direction = pageCriteria.direction() == SortDirection.ASC
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, pageCriteria.sortProperty());
        if (!"id".equals(pageCriteria.sortProperty())) {
            sort = sort.and(Sort.by(Sort.Direction.ASC, "id"));
        }

        PageRequest pageRequest = PageRequest.of(pageCriteria.page(), pageCriteria.size(), sort);
        Page<Customer> page = repository.findAll(specification(criteria), pageRequest);
        return new PageResult<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    private Specification<Customer> specification(CustomerSearchCriteria criteria) {
        Specification<Customer> specification = (root, query, builder) -> builder.conjunction();

        if (criteria.active() != null) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("active"), criteria.active()));
        }

        if (criteria.search() != null) {
            String pattern = "%" + escapeLike(criteria.search().toLowerCase(Locale.ROOT)) + "%";
            specification = specification.and((root, query, builder) -> {
                Expression<String> lowerName = builder.lower(root.get("name"));
                Expression<String> lowerDocument = builder.lower(root.get("documentNumber"));
                return builder.or(
                        builder.like(lowerName, pattern, '\\'),
                        builder.like(lowerDocument, pattern, '\\')
                );
            });
        }

        return specification;
    }

    private String escapeLike(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
