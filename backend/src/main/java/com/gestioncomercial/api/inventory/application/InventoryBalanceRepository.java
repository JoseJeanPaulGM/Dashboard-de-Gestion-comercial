package com.gestioncomercial.api.inventory.application;

public interface InventoryBalanceRepository {
    void increase(long productId, long quantity);
}
