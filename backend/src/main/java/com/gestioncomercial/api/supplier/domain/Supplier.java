package com.gestioncomercial.api.supplier.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "suppliers", uniqueConstraints = @UniqueConstraint(name = "uk_suppliers_ruc", columnNames = "ruc"))
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 11)
    private String ruc;

    @Column(name = "business_name", nullable = false, length = 150)
    private String businessName;

    @Column(name = "trade_name", length = 150)
    private String tradeName;

    @Column(length = 254)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(length = 250)
    private String address;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Supplier() {
    }

    public static Supplier create(String ruc, String businessName, String tradeName, String email,
                                  String phone, String address, Clock clock) {
        Supplier supplier = new Supplier();
        supplier.ruc = normalizeRequired(ruc);
        supplier.businessName = normalizeRequired(businessName);
        supplier.tradeName = normalizeOptional(tradeName);
        supplier.email = normalizeOptional(email);
        supplier.phone = normalizeOptional(phone);
        supplier.address = normalizeOptional(address);
        supplier.active = true;
        Instant now = now(clock);
        supplier.createdAt = now;
        supplier.updatedAt = now;
        return supplier;
    }

    public void update(String ruc, String businessName, String tradeName, String email,
                       String phone, String address, Clock clock) {
        this.ruc = normalizeRequired(ruc);
        this.businessName = normalizeRequired(businessName);
        this.tradeName = normalizeOptional(tradeName);
        this.email = normalizeOptional(email);
        this.phone = normalizeOptional(phone);
        this.address = normalizeOptional(address);
        this.updatedAt = now(clock);
    }

    public void changeActive(boolean active, Clock clock) {
        if (this.active != active) {
            this.active = active;
            this.updatedAt = now(clock);
        }
    }

    public static String normalizeRuc(String value) {
        return normalizeRequired(value);
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

    public Long getId() { return id; }
    public String getRuc() { return ruc; }
    public String getBusinessName() { return businessName; }
    public String getTradeName() { return tradeName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
