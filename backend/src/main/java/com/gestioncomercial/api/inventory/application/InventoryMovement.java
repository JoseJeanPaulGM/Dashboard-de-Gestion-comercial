package com.gestioncomercial.api.inventory.application;

import java.time.Instant;
import java.util.UUID;

public record InventoryMovement(long id, long productId, long quantityChange, long balanceAfter,
                                String sourceType, Long sourceId, UUID requestKey, String reason,
                                Instant createdAt) {
}
