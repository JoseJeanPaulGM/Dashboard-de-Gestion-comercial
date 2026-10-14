package com.gestioncomercial.api.inventory.application;

public class AdjustmentKeyConflictException extends RuntimeException {
    public AdjustmentKeyConflictException() {
        super("requestKey was already used for a different adjustment");
    }
}
