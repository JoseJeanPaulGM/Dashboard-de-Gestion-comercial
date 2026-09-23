package com.gestioncomercial.api.supplier.api;

import com.gestioncomercial.api.supplier.domain.Supplier;
import java.time.Instant;

public record SupplierResponse(Long id, String ruc, String businessName, String tradeName, String email,
                               String phone, String address, boolean active, Instant createdAt, Instant updatedAt) {
    static SupplierResponse from(Supplier supplier) {
        return new SupplierResponse(supplier.getId(), supplier.getRuc(), supplier.getBusinessName(),
                supplier.getTradeName(), supplier.getEmail(), supplier.getPhone(), supplier.getAddress(),
                supplier.isActive(), supplier.getCreatedAt(), supplier.getUpdatedAt());
    }
}
