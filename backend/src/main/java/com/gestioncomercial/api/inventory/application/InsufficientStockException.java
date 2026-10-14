package com.gestioncomercial.api.inventory.application;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException() {
        super("insufficient stock to confirm the sale");
    }
}
