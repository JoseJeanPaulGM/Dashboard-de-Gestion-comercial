package com.gestioncomercial.api.sale.domain;

import com.gestioncomercial.api.customer.domain.Customer;
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
@Table(name = "sales")
public class Sale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SaleStatus status;

    @Column(nullable = false, precision = 20, scale = 2)
    private BigDecimal total;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<SaleLine> lines = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    protected Sale() {
    }

    public static Sale create(Customer customer, List<SaleLineInput> inputs, Clock clock) {
        if (inputs == null || inputs.isEmpty() || inputs.size() > 100) {
            throw new IllegalArgumentException("sale must contain between 1 and 100 lines");
        }
        Sale sale = new Sale();
        sale.customer = customer;
        sale.status = SaleStatus.DRAFT;
        sale.createdAt = now(clock);
        sale.total = BigDecimal.ZERO.setScale(2);
        for (SaleLineInput input : inputs) {
            SaleLine line = SaleLine.create(sale, input.product(), input.quantity(), input.product().getSalePrice());
            sale.lines.add(line);
            sale.total = sale.total.add(line.getSubtotal());
        }
        if (sale.total.precision() - sale.total.scale() > 18) {
            throw new IllegalArgumentException("sale total exceeds the supported range");
        }
        return sale;
    }

    public void confirm(Clock clock) {
        if (status != SaleStatus.DRAFT) {
            throw new SaleStateConflictException();
        }
        status = SaleStatus.CONFIRMED;
        confirmedAt = now(clock);
    }

    private static Instant now(Clock clock) {
        return clock.instant().truncatedTo(ChronoUnit.MICROS);
    }

    public Long getId() { return id; }
    public Customer getCustomer() { return customer; }
    public SaleStatus getStatus() { return status; }
    public BigDecimal getTotal() { return total; }
    public List<SaleLine> getLines() { return Collections.unmodifiableList(lines); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getConfirmedAt() { return confirmedAt; }
}
