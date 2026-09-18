package com.gestioncomercial.api.customer.infrastructure;

import com.gestioncomercial.api.customer.domain.Customer;
import com.gestioncomercial.api.customer.domain.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface SpringDataCustomerRepository
        extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {

    boolean existsByDocumentTypeAndDocumentNumber(DocumentType documentType, String documentNumber);

    boolean existsByDocumentTypeAndDocumentNumberAndIdNot(
            DocumentType documentType,
            String documentNumber,
            long id
    );
}
