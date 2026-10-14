package com.gestioncomercial.api.purchase.domain;

import com.gestioncomercial.api.supplier.domain.Supplier;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "purchases")
public class Purchase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PurchaseStatus status;

    @Column(nullable = false, precision = 20, scale = 2)
    private BigDecimal total;

    @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<PurchaseLine> lines = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    protected Purchase() {
    }

    public static Purchase create(Supplier supplier, List<PurchaseLineInput> inputs, Clock clock) {
        if (inputs == null || inputs.isEmpty() || inputs.size() > 100) {
            throw new IllegalArgumentException("purchase must contain between 1 and 100 lines");
        }
        Purchase purchase = new Purchase();
        purchase.supplier = supplier;
        purchase.status = PurchaseStatus.DRAFT;
        purchase.createdAt = now(clock);
        purchase.total = BigDecimal.ZERO.setScale(2);
        for (PurchaseLineInput input : inputs) {
            PurchaseLine line = PurchaseLine.create(purchase, input.product(), input.quantity(), input.unitPrice());
            purchase.lines.add(line);
            purchase.total = purchase.total.add(line.getSubtotal());
        }
        if (purchase.total.precision() - purchase.total.scale() > 18) {
            throw new IllegalArgumentException("purchase total exceeds the supported range");
        }
        return purchase;
    }

    public void confirm(Clock clock) {
        if (status != PurchaseStatus.DRAFT) {
            throw new PurchaseStateConflictException();
        }
        status = PurchaseStatus.CONFIRMED;
        confirmedAt = now(clock);
    }

    private static Instant now(Clock clock) {
        return clock.instant().truncatedTo(ChronoUnit.MICROS);
    }

    public Long getId() { return id; }
    public Supplier getSupplier() { return supplier; }
    public PurchaseStatus getStatus() { return status; }
    public BigDecimal getTotal() { return total; }
    public List<PurchaseLine> getLines() { return Collections.unmodifiableList(lines); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getConfirmedAt() { return confirmedAt; }
}
