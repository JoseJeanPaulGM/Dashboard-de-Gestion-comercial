package com.gestioncomercial.api.customer.application;

import com.gestioncomercial.api.customer.domain.DocumentType;

public record UpdateCustomerCommand(
        DocumentType documentType,
        String documentNumber,
        String name,
        String email,
        String phone,
        String address
) {
}
