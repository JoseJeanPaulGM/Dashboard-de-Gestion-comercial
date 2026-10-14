package com.gestioncomercial.api.sale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.gestioncomercial.api.catalog.domain.Product;
import com.gestioncomercial.api.customer.domain.Customer;
import com.gestioncomercial.api.sale.domain.Sale;
import com.gestioncomercial.api.sale.domain.SaleLineInput;
import com.gestioncomercial.api.sale.domain.SaleStateConflictException;
import com.gestioncomercial.api.sale.domain.SaleStatus;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

class SaleDomainTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-13T12:00:00Z"), ZoneOffset.UTC);

    @Test
    void snapshotsCatalogPricesAndConfirmsOnlyOnce() {
        Product first = mock(Product.class);
        Product second = mock(Product.class);
        when(first.getSalePrice()).thenReturn(new BigDecimal("2.50"));
        when(second.getSalePrice()).thenReturn(new BigDecimal("1.25"));
        Sale sale = Sale.create(mock(Customer.class), List.of(
                new SaleLineInput(first, 3), new SaleLineInput(second, 2)), CLOCK);

        assertThat(sale.getStatus()).isEqualTo(SaleStatus.DRAFT);
        assertThat(sale.getTotal()).isEqualByComparingTo("10.00");
        assertThat(sale.getLines().get(0).getUnitPrice()).isEqualByComparingTo("2.50");
        assertThat(sale.getConfirmedAt()).isNull();

        sale.confirm(CLOCK);
        assertThat(sale.getStatus()).isEqualTo(SaleStatus.CONFIRMED);
        assertThat(sale.getConfirmedAt()).isEqualTo(CLOCK.instant());
        assertThatThrownBy(() -> sale.confirm(CLOCK)).isInstanceOf(SaleStateConflictException.class);
    }

    @Test
    void rejectsInvalidLineQuantity() {
        Product product = mock(Product.class);
        when(product.getSalePrice()).thenReturn(new BigDecimal("1.00"));
        assertThatThrownBy(() -> Sale.create(mock(Customer.class),
                List.of(new SaleLineInput(product, 0)), CLOCK))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
