package com.gestioncomercial.api.supplier.api;

import com.gestioncomercial.api.supplier.application.CreateSupplierCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateSupplierRequest(
        @NotBlank(message = "ruc must not be blank")
        @Pattern(regexp = "[0-9]{11}", message = "ruc must contain exactly 11 digits") String ruc,
        @NotBlank(message = "businessName must not be blank")
        @Size(max = 150, message = "businessName must not exceed 150 characters") String businessName,
        @Size(max = 150, message = "tradeName must not exceed 150 characters") String tradeName,
        @Email(message = "email must be valid")
        @Size(max = 254, message = "email must not exceed 254 characters") String email,
        @Size(max = 30, message = "phone must not exceed 30 characters") String phone,
        @Size(max = 250, message = "address must not exceed 250 characters") String address
) {
    CreateSupplierCommand toCommand() {
        return new CreateSupplierCommand(ruc, businessName, tradeName, email, phone, address);
    }
}
