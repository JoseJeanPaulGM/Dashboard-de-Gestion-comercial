package com.gestioncomercial.api.inventory.infrastructure;

import com.gestioncomercial.api.inventory.application.InventoryBalanceRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class InventoryBalancePersistenceAdapter implements InventoryBalanceRepository {
    private final JdbcTemplate jdbcTemplate;
    private final Clock clock;

    InventoryBalancePersistenceAdapter(JdbcTemplate jdbcTemplate, Clock clock) {
        this.jdbcTemplate = jdbcTemplate;
        this.clock = clock;
    }

    @Override
    public void increase(long productId, long quantity) {
        Instant now = clock.instant().truncatedTo(ChronoUnit.MICROS);
        jdbcTemplate.update("""
                INSERT INTO inventory_balances (product_id, quantity, updated_at)
                VALUES (?, ?, ?)
                ON CONFLICT (product_id) DO UPDATE
                SET quantity = inventory_balances.quantity + EXCLUDED.quantity,
                    updated_at = EXCLUDED.updated_at
                """, productId, quantity, OffsetDateTime.ofInstant(now, ZoneOffset.UTC));
    }
}
