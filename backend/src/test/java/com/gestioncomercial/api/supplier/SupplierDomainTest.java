package com.gestioncomercial.api.supplier;

import static org.assertj.core.api.Assertions.assertThat;

import com.gestioncomercial.api.catalog.domain.Category;
import com.gestioncomercial.api.catalog.domain.Product;
import com.gestioncomercial.api.supplier.domain.Supplier;
import com.gestioncomercial.api.supplier.domain.SupplierProduct;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class SupplierDomainTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-09-18T20:00:00.123456789Z"), ZoneOffset.UTC);

    @Test
    void normalizesSupplierAndProductLinkData() {
        Supplier supplier = Supplier.create(" 20123456789 ", " Acme Peru ", "  ", " sales@acme.test ",
                " 999-000 ", " Lima ", CLOCK);
        Category category = Category.create("OFFICE", "Office", null, CLOCK);
        Product product = Product.create("PEN-01", "Pen", null, BigDecimal.ONE, category, CLOCK);
        SupplierProduct link = SupplierProduct.create(supplier, product, " ACME-PEN ", CLOCK);

        assertThat(supplier.getRuc()).isEqualTo("20123456789");
        assertThat(supplier.getBusinessName()).isEqualTo("Acme Peru");
        assertThat(supplier.getTradeName()).isNull();
        assertThat(supplier.getEmail()).isEqualTo("sales@acme.test");
        assertThat(link.getSupplierProductCode()).isEqualTo("ACME-PEN");
        assertThat(link.getCreatedAt()).isEqualTo("2026-09-18T20:00:00.123456Z");
    }

    @Test
    void changesStatusesIdempotently() {
        Supplier supplier = Supplier.create("20123456789", "Acme", null, null, null, null, CLOCK);
        Category category = Category.create("OFFICE", "Office", null, CLOCK);
        Product product = Product.create("PEN-01", "Pen", null, BigDecimal.ONE, category, CLOCK);
        SupplierProduct link = SupplierProduct.create(supplier, product, null, CLOCK);
        Clock later = Clock.fixed(Instant.parse("2026-09-18T21:00:00Z"), ZoneOffset.UTC);

        supplier.changeActive(false, later);
        link.changeActive(false, later);
        Instant supplierUpdate = supplier.getUpdatedAt();
        Instant linkUpdate = link.getUpdatedAt();
        supplier.changeActive(false, Clock.offset(later, java.time.Duration.ofHours(1)));
        link.changeActive(false, Clock.offset(later, java.time.Duration.ofHours(1)));

        assertThat(supplier.getUpdatedAt()).isEqualTo(supplierUpdate);
        assertThat(link.getUpdatedAt()).isEqualTo(linkUpdate);
    }
}
