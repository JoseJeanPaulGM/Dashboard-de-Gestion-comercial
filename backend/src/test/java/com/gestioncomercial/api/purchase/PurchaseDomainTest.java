package com.gestioncomercial.api.purchase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.gestioncomercial.api.catalog.domain.Product;
import com.gestioncomercial.api.purchase.domain.Purchase;
import com.gestioncomercial.api.purchase.domain.PurchaseLineInput;
import com.gestioncomercial.api.purchase.domain.PurchaseStateConflictException;
import com.gestioncomercial.api.purchase.domain.PurchaseStatus;
import com.gestioncomercial.api.supplier.domain.Supplier;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

class PurchaseDomainTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-09T12:00:00Z"), ZoneOffset.UTC);

    @Test
    void calculatesMoneyAndConfirmsOnlyOnce() {
        Purchase purchase = Purchase.create(mock(Supplier.class), List.of(
                new PurchaseLineInput(mock(Product.class), 3, new BigDecimal("2.50")),
                new PurchaseLineInput(mock(Product.class), 2, new BigDecimal("1.25"))), CLOCK);

        assertThat(purchase.getStatus()).isEqualTo(PurchaseStatus.DRAFT);
        assertThat(purchase.getTotal()).isEqualByComparingTo("10.00");
        assertThat(purchase.getLines().get(0).getSubtotal()).isEqualByComparingTo("7.50");
        assertThat(purchase.getConfirmedAt()).isNull();

        purchase.confirm(CLOCK);
        assertThat(purchase.getStatus()).isEqualTo(PurchaseStatus.CONFIRMED);
        assertThat(purchase.getConfirmedAt()).isEqualTo(CLOCK.instant());
        assertThatThrownBy(() -> purchase.confirm(CLOCK)).isInstanceOf(PurchaseStateConflictException.class);
    }

    @Test
    void rejectsInvalidLineQuantityAndPrice() {
        Product product = mock(Product.class);
        Supplier supplier = mock(Supplier.class);
        assertThatThrownBy(() -> Purchase.create(supplier,
                List.of(new PurchaseLineInput(product, 0, new BigDecimal("1.00"))), CLOCK))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Purchase.create(supplier,
                List.of(new PurchaseLineInput(product, 1, new BigDecimal("0.00"))), CLOCK))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
