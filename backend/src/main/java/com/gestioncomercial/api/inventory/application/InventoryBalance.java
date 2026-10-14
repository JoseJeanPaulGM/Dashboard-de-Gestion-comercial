package com.gestioncomercial.api.inventory.application;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public record InventoryBalance(long productId, long quantity, long minimumQuantity, Instant updatedAt) {
    @JsonProperty("lowStock")
    public boolean lowStock() {
        return minimumQuantity > 0 && quantity <= minimumQuantity;
    }
}
