package com.gestioncomercial.api.catalog.domain;

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
import java.util.Locale;

@Entity
@Table(
        name = "categories",
        uniqueConstraints = @UniqueConstraint(name = "uk_categories_code", columnNames = "code")
)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 250)
    private String description;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Category() {
    }

    public static Category create(String code, String name, String description, Clock clock) {
        Category category = new Category();
        category.code = normalizeCode(code);
        category.name = normalizeRequired(name);
        category.description = normalizeOptional(description);
        category.active = true;
        Instant now = now(clock);
        category.createdAt = now;
        category.updatedAt = now;
        return category;
    }

    public void update(String code, String name, String description, Clock clock) {
        this.code = normalizeCode(code);
        this.name = normalizeRequired(name);
        this.description = normalizeOptional(description);
        this.updatedAt = now(clock);
    }

    public void changeActive(boolean active, Clock clock) {
        if (this.active != active) {
            this.active = active;
            this.updatedAt = now(clock);
        }
    }

    public static String normalizeCode(String value) {
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

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
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
