package com.gestioncomercial.api.customer;

import static org.assertj.core.api.Assertions.assertThat;

import com.gestioncomercial.api.customer.domain.Customer;
import com.gestioncomercial.api.customer.domain.DocumentType;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class CustomerDomainTest {

    private static final Instant CREATED_AT = Instant.parse("2026-09-18T12:00:00Z");
    private static final Clock CLOCK = Clock.fixed(CREATED_AT, ZoneOffset.UTC);

    @Test
    void createsAnActiveCustomerAndNormalizesItsFields() {
        Customer customer = Customer.create(
                DocumentType.OTHER,
                " ab-123 ",
                " Example customer ",
                " ",
                " +51 999 111 222 ",
                null,
                CLOCK
        );

        assertThat(customer.getDocumentNumber()).isEqualTo("AB-123");
        assertThat(customer.getName()).isEqualTo("Example customer");
        assertThat(customer.getEmail()).isNull();
        assertThat(customer.getPhone()).isEqualTo("+51 999 111 222");
        assertThat(customer.getAddress()).isNull();
        assertThat(customer.isActive()).isTrue();
        assertThat(customer.getCreatedAt()).isEqualTo(CREATED_AT);
        assertThat(customer.getUpdatedAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void changingToTheCurrentStatusIsIdempotent() {
        Customer customer = Customer.create(
                DocumentType.DNI, "12345678", "Example", null, null, null, CLOCK
        );

        customer.changeActive(true, Clock.fixed(CREATED_AT.plusSeconds(60), ZoneOffset.UTC));

        assertThat(customer.getUpdatedAt()).isEqualTo(CREATED_AT);
    }
}
