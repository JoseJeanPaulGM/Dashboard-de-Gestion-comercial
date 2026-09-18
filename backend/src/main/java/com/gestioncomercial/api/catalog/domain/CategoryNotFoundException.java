package com.gestioncomercial.api.catalog.domain;

public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(long id) {
        super("Category not found: " + id);
    }
}
