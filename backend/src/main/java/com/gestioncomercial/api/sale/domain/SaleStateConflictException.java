package com.gestioncomercial.api.sale.domain;

public class SaleStateConflictException extends RuntimeException {
    public SaleStateConflictException() {
        super("Only draft sales can be confirmed");
    }
}
