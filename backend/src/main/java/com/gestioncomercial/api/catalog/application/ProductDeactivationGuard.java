package com.gestioncomercial.api.catalog.application;

public interface ProductDeactivationGuard {

    void ensureCanDeactivate(long productId);
}
