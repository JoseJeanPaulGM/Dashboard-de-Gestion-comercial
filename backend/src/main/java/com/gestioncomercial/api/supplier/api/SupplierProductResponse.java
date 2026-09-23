package com.gestioncomercial.api.supplier.api;

import com.gestioncomercial.api.supplier.domain.SupplierProduct;
import java.time.Instant;

public record SupplierProductResponse(Long id, Long supplierId, Long productId, String sku, String productName,
                                      String supplierProductCode, boolean productActive, boolean active,
                                      Instant createdAt, Instant updatedAt) {
    static SupplierProductResponse from(SupplierProduct link) {
        return new SupplierProductResponse(link.getId(), link.getSupplier().getId(), link.getProduct().getId(),
                link.getProduct().getSku(), link.getProduct().getName(), link.getSupplierProductCode(),
                link.getProduct().isActive(), link.isActive(), link.getCreatedAt(), link.getUpdatedAt());
    }
}
