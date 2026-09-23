package com.gestioncomercial.api.supplier.application;

public record CreateSupplierCommand(String ruc, String businessName, String tradeName,
                                    String email, String phone, String address) {
}
