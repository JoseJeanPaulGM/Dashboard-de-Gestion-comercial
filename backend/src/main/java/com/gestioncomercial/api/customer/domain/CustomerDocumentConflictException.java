package com.gestioncomercial.api.customer.domain;

public class CustomerDocumentConflictException extends RuntimeException {

    public CustomerDocumentConflictException() {
        super("A customer with the same document already exists");
    }
}
