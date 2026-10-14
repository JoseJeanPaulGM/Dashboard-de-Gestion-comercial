package com.gestioncomercial.api.inventory.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryStockService {
    private final InventoryBalanceRepository balances;

    public InventoryStockService(InventoryBalanceRepository balances) {
        this.balances = balances;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void increase(long productId, long quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        balances.increase(productId, quantity);
    }
}
