package com.gestioncomercial.api.persistence;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "persistence_probe")
public class PersistenceProbe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "probe_value", nullable = false, length = 120)
    private String value;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected PersistenceProbe() {
    }

    public PersistenceProbe(String value, Instant createdAt) {
        this.value = value;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
