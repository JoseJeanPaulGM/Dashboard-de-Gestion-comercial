package com.gestioncomercial.api.catalog.domain;

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
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

@Entity
@Table(
        name = "products",
        uniqueConstraints = @UniqueConstraint(name = "uk_products_sku", columnNames = "sku")
)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String sku;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "sale_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal salePrice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Product() {
    }

    public static Product create(
            String sku,
            String name,
            String description,
            BigDecimal salePrice,
            Category category,
            Clock clock
    ) {
        Product product = new Product();
        product.sku = normalizeSku(sku);
        product.name = normalizeRequired(name);
        product.description = normalizeOptional(description);
        product.salePrice = normalizePrice(salePrice);
        product.category = category;
        product.active = true;
        Instant now = now(clock);
        product.createdAt = now;
        product.updatedAt = now;
        return product;
    }

    public void update(
            String sku,
            String name,
            String description,
            BigDecimal salePrice,
            Category category,
            Clock clock
    ) {
        this.sku = normalizeSku(sku);
        this.name = normalizeRequired(name);
        this.description = normalizeOptional(description);
        this.salePrice = normalizePrice(salePrice);
        this.category = category;
        this.updatedAt = now(clock);
    }

    public void changeActive(boolean active, Clock clock) {
        if (this.active != active) {
            this.active = active;
            this.updatedAt = now(clock);
        }
    }

    public static String normalizeSku(String value) {
        return normalizeRequired(value).toUpperCase(Locale.ROOT);
    }

    private static BigDecimal normalizePrice(BigDecimal value) {
        if (value.signum() <= 0) {
            throw new IllegalArgumentException("salePrice must be greater than zero");
        }
        return value.setScale(2, RoundingMode.UNNECESSARY);
    }

    private static String normalizeRequired(String value) {
        return value.trim();
    }

    private static String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static Instant now(Clock clock) {
        return clock.instant().truncatedTo(ChronoUnit.MICROS);
    }

    public Long getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public Category getCategory() {
        return category;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
