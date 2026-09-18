package com.gestioncomercial.api.customer.domain;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(long id) {
        super("Customer " + id + " was not found");
    }
}
