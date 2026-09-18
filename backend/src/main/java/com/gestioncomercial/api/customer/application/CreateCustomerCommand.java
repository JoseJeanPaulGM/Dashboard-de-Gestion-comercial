package com.gestioncomercial.api.customer.application;

import com.gestioncomercial.api.customer.domain.DocumentType;

public record CreateCustomerCommand(
        DocumentType documentType,
        String documentNumber,
        String name,
        String email,
        String phone,
        String address
) {
}
