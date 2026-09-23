package com.gestioncomercial.api.supplier.domain;

public class SupplierHasActiveProductsException extends RuntimeException {
    public SupplierHasActiveProductsException() { super("A supplier with active product links cannot be deactivated"); }
}
