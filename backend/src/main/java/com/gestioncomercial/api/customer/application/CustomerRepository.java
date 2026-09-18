package com.gestioncomercial.api.customer.application;

import com.gestioncomercial.api.customer.domain.Customer;
import com.gestioncomercial.api.customer.domain.DocumentType;
import com.gestioncomercial.api.shared.pagination.PageResult;
import java.util.Optional;

public interface CustomerRepository {

    Customer save(Customer customer);

    Optional<Customer> findById(long id);

    boolean existsByDocument(DocumentType documentType, String documentNumber);

    boolean existsByDocumentExcludingId(DocumentType documentType, String documentNumber, long id);

    PageResult<Customer> findAll(CustomerSearchCriteria criteria);
}
