package com.gestioncomercial.api.supplier.domain;

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
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "supplier_products", uniqueConstraints = @UniqueConstraint(
        name = "uk_supplier_products_supplier_product", columnNames = {"supplier_id", "product_id"}))
public class SupplierProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "supplier_product_code", length = 80)
    private String supplierProductCode;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected SupplierProduct() {
    }

    public static SupplierProduct create(Supplier supplier, Product product, String supplierProductCode, Clock clock) {
        SupplierProduct link = new SupplierProduct();
        link.supplier = supplier;
        link.product = product;
        link.supplierProductCode = normalizeOptional(supplierProductCode);
        link.active = true;
        Instant now = now(clock);
        link.createdAt = now;
        link.updatedAt = now;
        return link;
    }

    public void updateCode(String supplierProductCode, Clock clock) {
        this.supplierProductCode = normalizeOptional(supplierProductCode);
        this.updatedAt = now(clock);
    }

    public void changeActive(boolean active, Clock clock) {
        if (this.active != active) {
            this.active = active;
            this.updatedAt = now(clock);
        }
    }

    private static String normalizeOptional(String value) {
        if (value == null) return null;
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static Instant now(Clock clock) {
        return clock.instant().truncatedTo(ChronoUnit.MICROS);
    }

    public Long getId() { return id; }
    public Supplier getSupplier() { return supplier; }
    public Product getProduct() { return product; }
    public String getSupplierProductCode() { return supplierProductCode; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
