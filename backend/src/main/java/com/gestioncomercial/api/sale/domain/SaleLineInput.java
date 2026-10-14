package com.gestioncomercial.api.sale.domain;

import com.gestioncomercial.api.catalog.domain.Product;
public record SaleLineInput(Product product, int quantity) {
}
