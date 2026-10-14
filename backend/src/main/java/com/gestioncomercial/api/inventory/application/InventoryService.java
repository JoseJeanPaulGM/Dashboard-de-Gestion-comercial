package com.gestioncomercial.api.inventory.application;

import com.gestioncomercial.api.catalog.application.ProductService;
import com.gestioncomercial.api.shared.api.InvalidRequestException;
import com.gestioncomercial.api.shared.pagination.PageResult;
import com.gestioncomercial.api.shared.pagination.Pagination;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class InventoryService {
    private static final Map<String, String> MOVEMENT_SORTS = Map.of(
            "id", "id", "createdAt", "created_at");
    private static final Map<String, String> ALERT_SORTS = Map.of(
            "productId", "product_id", "quantity", "quantity");

    private final InventoryRepository inventory;
    private final ProductService products;

    public InventoryService(InventoryRepository inventory, ProductService products) {
        this.inventory = inventory;
        this.products = products;
    }

    public InventoryBalance balance(long productId) {
        products.get(productId);
        return inventory.getBalance(productId);
    }

    public PageResult<InventoryMovement> movements(long productId, int page, int size, String sort) {
        products.get(productId);
        return inventory.movements(productId, Pagination.parse(page, size, sort, MOVEMENT_SORTS));
    }

    public PageResult<InventoryBalance> alerts(int page, int size, String sort) {
        return inventory.alerts(Pagination.parse(page, size, sort, ALERT_SORTS));
    }

    @Transactional
    public InventoryBalance setMinimum(long productId, long minimum) {
        if (minimum < 0) throw new InvalidRequestException("minimumQuantity must be non-negative");
        products.get(productId);
        inventory.setMinimum(productId, minimum);
        return inventory.getBalance(productId);
    }

    @Transactional
    public InventoryMovement adjust(long productId, long change, String reason, UUID requestKey) {
        if (requestKey == null) throw new InvalidRequestException("requestKey is required");
        if (change == 0 || change < -1_000_000 || change > 1_000_000) {
            throw new InvalidRequestException("quantityChange must be between -1000000 and 1000000, excluding zero");
        }
        if (reason == null || reason.trim().length() < 10 || reason.trim().length() > 500) {
            throw new InvalidRequestException("reason must contain between 10 and 500 characters");
        }
        String normalizedReason = reason.trim();
        inventory.lockRequestKey(requestKey);
        var previous = inventory.findByRequestKey(requestKey);
        if (previous.isPresent()) {
            InventoryMovement movement = previous.get();
            if (movement.productId() != productId || movement.quantityChange() != change
                    || !normalizedReason.equals(movement.reason())) {
                throw new AdjustmentKeyConflictException();
            }
            return movement;
        }
        products.get(productId);
        long balance = change > 0 ? inventory.increase(productId, change)
                : inventory.decrease(productId, -change).orElseThrow(InsufficientStockException::new);
        inventory.recordMovement(productId, change, balance, "ADJUSTMENT", null, requestKey,
                normalizedReason);
        return inventory.findByRequestKey(requestKey).orElseThrow();
    }
}
