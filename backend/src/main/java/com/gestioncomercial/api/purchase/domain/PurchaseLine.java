package com.gestioncomercial.api.purchase.domain;

import com.gestioncomercial.api.catalog.domain.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "purchase_lines", uniqueConstraints = @UniqueConstraint(
        name = "uk_purchase_lines_purchase_product", columnNames = {"purchase_id", "product_id"}))
public class PurchaseLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_id", nullable = false)
    private Purchase purchase;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 20, scale = 2)
    private BigDecimal subtotal;

    protected PurchaseLine() {
    }

    static PurchaseLine create(Purchase purchase, Product product, int quantity, BigDecimal unitPrice) {
        if (quantity < 1 || quantity > 1_000_000) {
            throw new IllegalArgumentException("quantity must be between 1 and 1000000");
        }
        if (unitPrice == null || unitPrice.signum() <= 0 || unitPrice.precision() - unitPrice.scale() > 10) {
            throw new IllegalArgumentException("unitPrice must be positive and fit NUMERIC(12,2)");
        }
        PurchaseLine line = new PurchaseLine();
        line.purchase = purchase;
        line.product = product;
        line.quantity = quantity;
        line.unitPrice = unitPrice.setScale(2, RoundingMode.UNNECESSARY);
        line.subtotal = line.unitPrice.multiply(BigDecimal.valueOf(quantity));
        return line;
    }

    public Long getId() { return id; }
    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getSubtotal() { return subtotal; }
}
