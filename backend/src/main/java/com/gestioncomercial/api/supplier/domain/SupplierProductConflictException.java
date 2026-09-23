package com.gestioncomercial.api.supplier.domain;

public class SupplierProductConflictException extends RuntimeException {
    public SupplierProductConflictException() { super("The product is already linked to this supplier"); }
}
