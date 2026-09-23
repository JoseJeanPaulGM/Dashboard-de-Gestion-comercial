package com.gestioncomercial.api.supplier.domain;

public class SupplierNotFoundException extends RuntimeException {
    public SupplierNotFoundException(long id) { super("Supplier " + id + " was not found"); }
}
