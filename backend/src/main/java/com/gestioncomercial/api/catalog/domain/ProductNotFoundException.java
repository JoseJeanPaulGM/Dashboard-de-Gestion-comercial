package com.gestioncomercial.api.catalog.domain;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(long id) {
        super("Product not found: " + id);
    }
}
