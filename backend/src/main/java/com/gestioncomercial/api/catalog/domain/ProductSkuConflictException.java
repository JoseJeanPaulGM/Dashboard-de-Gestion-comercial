package com.gestioncomercial.api.catalog.domain;

public class ProductSkuConflictException extends RuntimeException {

    public ProductSkuConflictException() {
        super("A product with the same SKU already exists");
    }
}
