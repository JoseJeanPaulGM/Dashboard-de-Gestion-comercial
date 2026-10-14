package com.gestioncomercial.api.purchase.domain;

public class PurchaseStateConflictException extends RuntimeException {
    public PurchaseStateConflictException() {
        super("Only draft purchases can be confirmed");
    }
}
