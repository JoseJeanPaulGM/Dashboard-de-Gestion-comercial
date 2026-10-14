package com.gestioncomercial.api.purchase.application;

import com.gestioncomercial.api.catalog.application.ProductService;
import com.gestioncomercial.api.catalog.domain.Product;
import com.gestioncomercial.api.inventory.application.InventoryStockService;
import com.gestioncomercial.api.purchase.domain.Purchase;
import com.gestioncomercial.api.purchase.domain.PurchaseLineInput;
import com.gestioncomercial.api.purchase.domain.PurchaseNotFoundException;
import com.gestioncomercial.api.purchase.domain.PurchaseStatus;
import com.gestioncomercial.api.purchase.domain.PurchaseStateConflictException;
import com.gestioncomercial.api.purchase.domain.PurchaseSupplyUnavailableException;
import com.gestioncomercial.api.shared.api.InvalidRequestException;
import com.gestioncomercial.api.shared.pagination.PageResult;
import com.gestioncomercial.api.shared.pagination.Pagination;
import com.gestioncomercial.api.supplier.application.SupplierService;
import com.gestioncomercial.api.supplier.domain.Supplier;
import java.time.Clock;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PurchaseService {
    private static final Map<String, String> SORTS = Map.of(
            "id", "id", "supplierId", "supplier.id", "status", "status",
            "total", "total", "createdAt", "createdAt", "confirmedAt", "confirmedAt");

    private final PurchaseRepository purchases;
    private final SupplierService suppliers;
    private final ProductService products;
    private final InventoryStockService stock;
    private final Clock clock;

    public PurchaseService(PurchaseRepository purchases, SupplierService suppliers, ProductService products,
                           InventoryStockService stock, Clock clock) {
        this.purchases = purchases;
        this.suppliers = suppliers;
        this.products = products;
        this.stock = stock;
        this.clock = clock;
    }

    @Transactional
    public Purchase create(CreatePurchaseCommand command) {
        Supplier supplier = suppliers.get(command.supplierId());
        if (!supplier.isActive()) throw new PurchaseSupplyUnavailableException();
        if (command.lines() == null || command.lines().isEmpty() || command.lines().size() > 100) {
            throw new InvalidRequestException("purchase must contain between 1 and 100 lines");
        }
        Set<Long> seen = new HashSet<>();
        List<PurchaseLineInput> inputs = new ArrayList<>();
        for (CreatePurchaseCommand.Line line : command.lines()) {
            if (!seen.add(line.productId())) {
                throw new InvalidRequestException("each product can appear only once in a purchase");
            }
            Product product = products.get(line.productId());
            ensureSupplyAvailable(supplier, product);
            inputs.add(new PurchaseLineInput(product, line.quantity(), line.unitPrice()));
        }
        return purchases.save(Purchase.create(supplier, inputs, clock));
    }

    public Purchase get(long id) {
        return purchases.findById(id).orElseThrow(() -> new PurchaseNotFoundException(id));
    }

    public PageResult<Purchase> list(int page, int size, String sort, PurchaseStatus status, Long supplierId) {
        if (supplierId != null && supplierId <= 0) {
            throw new InvalidRequestException("supplierId must be greater than zero");
        }
        return purchases.findAll(new PurchaseSearchCriteria(status, supplierId,
                Pagination.parse(page, size, sort, SORTS)));
    }

    @Transactional
    public Purchase confirm(long id) {
        Purchase purchase = purchases.findByIdForUpdate(id)
                .orElseThrow(() -> new PurchaseNotFoundException(id));
        // Check the state before any stock mutation; the row lock serializes repeated confirmations.
        if (purchase.getStatus() != PurchaseStatus.DRAFT) {
            throw new PurchaseStateConflictException();
        }
        for (var line : purchase.getLines()) {
            ensureSupplyAvailable(purchase.getSupplier(), line.getProduct());
        }
        purchase.getLines().stream()
                .sorted((left, right) -> Long.compare(left.getProduct().getId(), right.getProduct().getId()))
                .forEach(line -> stock.recordPurchase(line.getProduct().getId(), line.getQuantity(), line.getId()));
        purchase.confirm(clock);
        return purchases.save(purchase);
    }

    private void ensureSupplyAvailable(Supplier supplier, Product product) {
        if (!supplier.isActive() || !product.isActive()
                || !suppliers.hasActiveProductLink(supplier.getId(), product.getId())) {
            throw new PurchaseSupplyUnavailableException();
        }
    }
}
