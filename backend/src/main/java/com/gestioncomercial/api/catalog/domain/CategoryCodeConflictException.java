package com.gestioncomercial.api.catalog.domain;

public class CategoryCodeConflictException extends RuntimeException {

    public CategoryCodeConflictException() {
        super("A category with the same code already exists");
    }
}
