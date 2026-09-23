package com.gestioncomercial.api.supplier.domain;

public class SupplierRucConflictException extends RuntimeException {
    public SupplierRucConflictException() { super("A supplier with the same RUC already exists"); }
}
