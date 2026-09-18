package com.gestioncomercial.api.shared.pagination;

public class InvalidSortException extends RuntimeException {

    public InvalidSortException(String message) {
        super(message);
    }
}
