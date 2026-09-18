package com.gestioncomercial.api.customer.api;

import com.gestioncomercial.api.customer.domain.Customer;
import com.gestioncomercial.api.customer.domain.DocumentType;
import java.time.Instant;

public record CustomerResponse(
        Long id,
        DocumentType documentType,
        String documentNumber,
        String name,
        String email,
        String phone,
        String address,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {

    static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getDocumentType(),
                customer.getDocumentNumber(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getAddress(),
                customer.isActive(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }
}
