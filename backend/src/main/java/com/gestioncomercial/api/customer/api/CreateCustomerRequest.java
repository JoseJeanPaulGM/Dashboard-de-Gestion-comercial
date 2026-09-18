package com.gestioncomercial.api.customer.api;

import com.gestioncomercial.api.customer.application.CreateCustomerCommand;
import com.gestioncomercial.api.customer.domain.DocumentType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCustomerRequest(
        @NotNull(message = "documentType must not be null")
        DocumentType documentType,

        @NotBlank(message = "documentNumber must not be blank")
        @Size(max = 30, message = "documentNumber must not exceed 30 characters")
        String documentNumber,

        @NotBlank(message = "name must not be blank")
        @Size(max = 150, message = "name must not exceed 150 characters")
        String name,

        @Email(message = "email must be a valid email address")
        @Size(max = 254, message = "email must not exceed 254 characters")
        String email,

        @Size(max = 30, message = "phone must not exceed 30 characters")
        String phone,

        @Size(max = 250, message = "address must not exceed 250 characters")
        String address
) {

    CreateCustomerCommand toCommand() {
        return new CreateCustomerCommand(documentType, documentNumber, name, email, phone, address);
    }
}
