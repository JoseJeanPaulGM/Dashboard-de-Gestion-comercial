package com.gestioncomercial.api.supplier.domain;

public class SupplierProductNotFoundException extends RuntimeException {
    public SupplierProductNotFoundException(long supplierId, long productId) {
        super("Product " + productId + " is not linked to supplier " + supplierId);
    }
}
