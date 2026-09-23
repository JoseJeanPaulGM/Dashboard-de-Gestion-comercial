package com.gestioncomercial.api.catalog.domain;

public class ProductHasActiveSuppliersException extends RuntimeException {

    public ProductHasActiveSuppliersException() {
        super("A product with active supplier links cannot be deactivated");
    }
}
