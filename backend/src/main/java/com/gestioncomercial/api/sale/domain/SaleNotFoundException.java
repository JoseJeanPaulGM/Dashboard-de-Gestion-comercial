package com.gestioncomercial.api.sale.domain;

public class SaleNotFoundException extends RuntimeException {
    public SaleNotFoundException(long id) {
        super("Sale not found: " + id);
    }
}
