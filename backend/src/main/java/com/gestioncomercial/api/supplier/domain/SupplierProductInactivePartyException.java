package com.gestioncomercial.api.supplier.domain;

public class SupplierProductInactivePartyException extends RuntimeException {
    public SupplierProductInactivePartyException() { super("An active supplier and product are required for an active link"); }
}
