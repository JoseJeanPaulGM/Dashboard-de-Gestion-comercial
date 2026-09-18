package com.gestioncomercial.api.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gestioncomercial.api.catalog.domain.Category;
import com.gestioncomercial.api.catalog.domain.Product;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class CatalogDomainTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-09-18T15:30:00.123456789Z"), ZoneOffset.UTC);

    @Test
    void normalizesCategoryAndProductData() {
        Category category = Category.create(" office ", " Office supplies ", "  ", CLOCK);
        Product product = Product.create(
                " pen-01 ",
                " Blue pen ",
                "  Ballpoint  ",
                new BigDecimal("2.5"),
                category,
                CLOCK
        );

        assertThat(category.getCode()).isEqualTo("OFFICE");
        assertThat(category.getName()).isEqualTo("Office supplies");
        assertThat(category.getDescription()).isNull();
        assertThat(product.getSku()).isEqualTo("PEN-01");
        assertThat(product.getName()).isEqualTo("Blue pen");
        assertThat(product.getDescription()).isEqualTo("Ballpoint");
        assertThat(product.getSalePrice()).isEqualByComparingTo("2.50");
        assertThat(product.getCreatedAt()).isEqualTo("2026-09-18T15:30:00.123456Z");
    }

    @Test
    void rejectsNonPositiveOrOverPrecisePrices() {
        Category category = Category.create("OFFICE", "Office", null, CLOCK);

        assertThatThrownBy(() -> Product.create(
                "PEN-01", "Pen", null, BigDecimal.ZERO, category, CLOCK
        )).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Product.create(
                "PEN-01", "Pen", null, new BigDecimal("2.555"), category, CLOCK
        )).isInstanceOf(ArithmeticException.class);
    }

    @Test
    void changesStatusesIdempotently() {
        Category category = Category.create("OFFICE", "Office", null, CLOCK);
        Product product = Product.create("PEN-01", "Pen", null, BigDecimal.ONE, category, CLOCK);
        Clock later = Clock.fixed(Instant.parse("2026-09-18T16:00:00Z"), ZoneOffset.UTC);

        category.changeActive(false, later);
        product.changeActive(false, later);
        Instant categoryUpdatedAt = category.getUpdatedAt();
        Instant productUpdatedAt = product.getUpdatedAt();
        category.changeActive(false, Clock.offset(later, java.time.Duration.ofHours(1)));
        product.changeActive(false, Clock.offset(later, java.time.Duration.ofHours(1)));

        assertThat(categoryUpdatedAt).isEqualTo(category.getUpdatedAt());
        assertThat(productUpdatedAt).isEqualTo(product.getUpdatedAt());
    }
}
