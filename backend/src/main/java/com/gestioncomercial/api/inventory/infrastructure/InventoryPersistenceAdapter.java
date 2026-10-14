package com.gestioncomercial.api.inventory.infrastructure;

import com.gestioncomercial.api.inventory.application.InventoryBalance;
import com.gestioncomercial.api.inventory.application.InventoryMovement;
import com.gestioncomercial.api.inventory.application.InventoryRepository;
import com.gestioncomercial.api.shared.pagination.PageCriteria;
import com.gestioncomercial.api.shared.pagination.PageResult;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class InventoryPersistenceAdapter implements InventoryRepository {
    private final JdbcTemplate jdbc;
    private final Clock clock;

    InventoryPersistenceAdapter(JdbcTemplate jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    @Override
    public long increase(long productId, long quantity) {
        List<Long> result = jdbc.query("""
                INSERT INTO inventory_balances (product_id, quantity, updated_at)
                VALUES (?, ?, ?)
                ON CONFLICT (product_id) DO UPDATE
                SET quantity = inventory_balances.quantity + EXCLUDED.quantity,
                    updated_at = EXCLUDED.updated_at
                WHERE inventory_balances.quantity <= 9223372036854775807 - EXCLUDED.quantity
                RETURNING quantity
                """, (rs, row) -> rs.getLong(1), productId, quantity, now());
        if (result.isEmpty()) throw new IllegalStateException("inventory quantity exceeds BIGINT range");
        return result.get(0);
    }

    @Override
    public Optional<Long> decrease(long productId, long quantity) {
        List<Long> result = jdbc.query("""
                UPDATE inventory_balances
                SET quantity = quantity - ?, updated_at = ?
                WHERE product_id = ? AND quantity >= ?
                RETURNING quantity
                """, (rs, row) -> rs.getLong(1), quantity, now(), productId, quantity);
        return result.stream().findFirst();
    }

    @Override
    public void recordMovement(long productId, long change, long balanceAfter, String sourceType,
                               Long sourceId, UUID requestKey, String reason) {
        jdbc.update("""
                INSERT INTO inventory_movements
                    (product_id, quantity_change, balance_after, source_type, source_id,
                     request_key, reason, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """, productId, change, balanceAfter, sourceType, sourceId, requestKey, reason, now());
    }

    @Override
    public Optional<InventoryMovement> findByRequestKey(UUID requestKey) {
        return jdbc.query("SELECT * FROM inventory_movements WHERE request_key = ?",
                (rs, row) -> movement(rs), requestKey).stream().findFirst();
    }

    @Override
    public void lockRequestKey(UUID requestKey) {
        jdbc.query("SELECT pg_advisory_xact_lock(hashtextextended(?, 0))",
                (ResultSet rs) -> null, requestKey.toString());
    }

    @Override
    public InventoryBalance getBalance(long productId) {
        return jdbc.query("SELECT * FROM inventory_balances WHERE product_id = ?",
                (rs, row) -> balance(rs), productId).stream().findFirst()
                .orElse(new InventoryBalance(productId, 0, 0, null));
    }

    @Override
    public void setMinimum(long productId, long minimum) {
        jdbc.update("""
                INSERT INTO inventory_balances (product_id, quantity, minimum_quantity, updated_at)
                VALUES (?, 0, ?, ?)
                ON CONFLICT (product_id) DO UPDATE
                SET minimum_quantity = EXCLUDED.minimum_quantity,
                    updated_at = EXCLUDED.updated_at
                """, productId, minimum, now());
    }

    @Override
    public PageResult<InventoryMovement> movements(long productId, PageCriteria page) {
        long total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM inventory_movements WHERE product_id = ?", Long.class, productId);
        String order = page.sortProperty().equals("id") ? "id" : "created_at";
        String direction = page.direction().name();
        List<InventoryMovement> rows = jdbc.query("""
                SELECT * FROM inventory_movements WHERE product_id = ?
                ORDER BY %s %s, id %s LIMIT ? OFFSET ?
                """.formatted(order, direction, direction), (rs, row) -> movement(rs),
                productId, page.size(), (long) page.page() * page.size());
        return pageResult(rows, page, total);
    }

    @Override
    public PageResult<InventoryBalance> alerts(PageCriteria page) {
        String predicate = "minimum_quantity > 0 AND quantity <= minimum_quantity";
        long total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM inventory_balances WHERE " + predicate, Long.class);
        String order = page.sortProperty().equals("quantity") ? "quantity" : "product_id";
        String direction = page.direction().name();
        List<InventoryBalance> rows = jdbc.query("""
                SELECT * FROM inventory_balances WHERE %s
                ORDER BY %s %s, product_id ASC LIMIT ? OFFSET ?
                """.formatted(predicate, order, direction), (rs, row) -> balance(rs),
                page.size(), (long) page.page() * page.size());
        return pageResult(rows, page, total);
    }

    private <T> PageResult<T> pageResult(List<T> rows, PageCriteria page, long total) {
        return new PageResult<>(rows, page.page(), page.size(), total,
                (int) ((total + page.size() - 1) / page.size()));
    }

    private InventoryBalance balance(ResultSet rs) throws SQLException {
        return new InventoryBalance(rs.getLong("product_id"), rs.getLong("quantity"),
                rs.getLong("minimum_quantity"), instant(rs, "updated_at"));
    }

    private InventoryMovement movement(ResultSet rs) throws SQLException {
        Long sourceId = rs.getObject("source_id", Long.class);
        return new InventoryMovement(rs.getLong("id"), rs.getLong("product_id"),
                rs.getLong("quantity_change"), rs.getLong("balance_after"),
                rs.getString("source_type"), sourceId, rs.getObject("request_key", UUID.class),
                rs.getString("reason"), instant(rs, "created_at"));
    }

    private Instant instant(ResultSet rs, String column) throws SQLException {
        return rs.getObject(column, OffsetDateTime.class).toInstant();
    }

    private OffsetDateTime now() {
        return OffsetDateTime.ofInstant(clock.instant().truncatedTo(ChronoUnit.MICROS), ZoneOffset.UTC);
    }
}
