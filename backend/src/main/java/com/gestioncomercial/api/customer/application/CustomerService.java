package com.gestioncomercial.api.customer.application;

import com.gestioncomercial.api.customer.domain.Customer;
import com.gestioncomercial.api.customer.domain.CustomerDocumentConflictException;
import com.gestioncomercial.api.customer.domain.CustomerNotFoundException;
import com.gestioncomercial.api.customer.domain.DocumentType;
import com.gestioncomercial.api.shared.api.InvalidRequestException;
import com.gestioncomercial.api.shared.pagination.PageCriteria;
import com.gestioncomercial.api.shared.pagination.PageResult;
import com.gestioncomercial.api.shared.pagination.Pagination;
import java.time.Clock;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CustomerService {

    private static final Map<String, String> SORT_PROPERTIES = Map.of(
            "id", "id",
            "name", "name",
            "documentNumber", "documentNumber",
            "active", "active",
            "createdAt", "createdAt",
            "updatedAt", "updatedAt"
    );

    private final CustomerRepository repository;
    private final Clock clock;

    public CustomerService(CustomerRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public Customer create(CreateCustomerCommand command) {
        String documentNumber = Customer.normalizeDocumentNumber(command.documentNumber());
        ensureDocumentAvailable(command.documentType(), documentNumber);
        return repository.save(Customer.create(
                command.documentType(),
                documentNumber,
                command.name(),
                command.email(),
                command.phone(),
                command.address(),
                clock
        ));
    }

    public Customer get(long id) {
        return repository.findById(id).orElseThrow(() -> new CustomerNotFoundException(id));
    }

    public PageResult<Customer> list(int page, int size, String sort, Boolean active, String search) {
        PageCriteria pageCriteria = Pagination.parse(page, size, sort, SORT_PROPERTIES);
        String normalizedSearch = normalizeSearch(search);
        return repository.findAll(new CustomerSearchCriteria(active, normalizedSearch, pageCriteria));
    }

    @Transactional
    public Customer update(long id, UpdateCustomerCommand command) {
        Customer customer = get(id);
        String documentNumber = Customer.normalizeDocumentNumber(command.documentNumber());
        if (repository.existsByDocumentExcludingId(command.documentType(), documentNumber, id)) {
            throw new CustomerDocumentConflictException();
        }
        customer.update(
                command.documentType(),
                documentNumber,
                command.name(),
                command.email(),
                command.phone(),
                command.address(),
                clock
        );
        return repository.save(customer);
    }

    @Transactional
    public Customer changeStatus(long id, boolean active) {
        Customer customer = get(id);
        customer.changeActive(active, clock);
        return repository.save(customer);
    }

    private void ensureDocumentAvailable(DocumentType type, String number) {
        if (repository.existsByDocument(type, number)) {
            throw new CustomerDocumentConflictException();
        }
    }

    private String normalizeSearch(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        String normalized = search.trim();
        if (normalized.length() > 150) {
            throw new InvalidRequestException("search must not exceed 150 characters");
        }
        return normalized;
    }
}
