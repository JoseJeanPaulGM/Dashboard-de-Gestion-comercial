package com.gestioncomercial.api.customer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

@Entity
@Table(
        name = "customers",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_customers_document",
                columnNames = {"document_type", "document_number"}
        )
)
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 20)
    private DocumentType documentType;

    @Column(name = "document_number", nullable = false, length = 30)
    private String documentNumber;

    @Column(nullable = false, length = 150)
    private String name;

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

    protected Customer() {
    }

    public static Customer create(
            DocumentType documentType,
            String documentNumber,
            String name,
            String email,
            String phone,
            String address,
            Clock clock
    ) {
        Customer customer = new Customer();
        customer.documentType = documentType;
        customer.documentNumber = normalizeDocumentNumber(documentNumber);
        customer.name = normalizeRequired(name);
        customer.email = normalizeOptional(email);
        customer.phone = normalizeOptional(phone);
        customer.address = normalizeOptional(address);
        customer.active = true;
        Instant now = now(clock);
        customer.createdAt = now;
        customer.updatedAt = now;
        return customer;
    }

    public void update(
            DocumentType documentType,
            String documentNumber,
            String name,
            String email,
            String phone,
            String address,
            Clock clock
    ) {
        this.documentType = documentType;
        this.documentNumber = normalizeDocumentNumber(documentNumber);
        this.name = normalizeRequired(name);
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

    public static String normalizeDocumentNumber(String value) {
        return normalizeRequired(value).toUpperCase(Locale.ROOT);
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

    public DocumentType getDocumentType() {
        return documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
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
