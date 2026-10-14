package com.gestioncomercial.api.inventory.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryStockService {
    private final InventoryRepository inventory;

    public InventoryStockService(InventoryRepository inventory) {
        this.inventory = inventory;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void recordPurchase(long productId, long quantity, long purchaseLineId) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        long balance = inventory.increase(productId, quantity);
        inventory.recordMovement(productId, quantity, balance, "PURCHASE", purchaseLineId, null, null);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void recordSale(long productId, long quantity, long saleLineId) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        long balance = inventory.decrease(productId, quantity)
                .orElseThrow(InsufficientStockException::new);
        inventory.recordMovement(productId, -quantity, balance, "SALE", saleLineId, null, null);
    }
}
