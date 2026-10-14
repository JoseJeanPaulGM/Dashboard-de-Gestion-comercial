package com.gestioncomercial.api.inventory.application;

import com.gestioncomercial.api.shared.pagination.PageCriteria;
import com.gestioncomercial.api.shared.pagination.PageResult;
import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository {
    long increase(long productId, long quantity);
    Optional<Long> decrease(long productId, long quantity);
    void recordMovement(long productId, long change, long balanceAfter, String sourceType,
                        Long sourceId, UUID requestKey, String reason);
    Optional<InventoryMovement> findByRequestKey(UUID requestKey);
    void lockRequestKey(UUID requestKey);
    InventoryBalance getBalance(long productId);
    void setMinimum(long productId, long minimum);
    PageResult<InventoryMovement> movements(long productId, PageCriteria page);
    PageResult<InventoryBalance> alerts(PageCriteria page);
}
