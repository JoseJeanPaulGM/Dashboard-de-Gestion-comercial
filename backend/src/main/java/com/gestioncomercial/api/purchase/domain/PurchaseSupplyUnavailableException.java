package com.gestioncomercial.api.purchase.domain;

public class PurchaseSupplyUnavailableException extends RuntimeException {
    public PurchaseSupplyUnavailableException() {
        super("The supplier, product, or supplier-product link is inactive or unavailable");
    }
}
