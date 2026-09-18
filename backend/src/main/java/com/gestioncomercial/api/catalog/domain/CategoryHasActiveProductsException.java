package com.gestioncomercial.api.catalog.domain;

public class CategoryHasActiveProductsException extends RuntimeException {

    public CategoryHasActiveProductsException() {
        super("A category with active products cannot be deactivated");
    }
}
