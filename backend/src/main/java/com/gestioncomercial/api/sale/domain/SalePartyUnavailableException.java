package com.gestioncomercial.api.sale.domain;

public class SalePartyUnavailableException extends RuntimeException {
    public SalePartyUnavailableException() {
        super("customer and products must be active to create or confirm a sale");
    }
}
