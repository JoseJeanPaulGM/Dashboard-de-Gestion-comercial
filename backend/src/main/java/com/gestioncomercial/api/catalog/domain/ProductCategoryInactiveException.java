package com.gestioncomercial.api.catalog.domain;

public class ProductCategoryInactiveException extends RuntimeException {

    public ProductCategoryInactiveException() {
        super("An active product must belong to an active category");
    }
}
