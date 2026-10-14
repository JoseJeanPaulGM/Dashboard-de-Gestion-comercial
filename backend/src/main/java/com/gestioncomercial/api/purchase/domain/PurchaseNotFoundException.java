package com.gestioncomercial.api.purchase.domain;

public class PurchaseNotFoundException extends RuntimeException {
    public PurchaseNotFoundException(long id) {
        super("Purchase not found: " + id);
    }
}
