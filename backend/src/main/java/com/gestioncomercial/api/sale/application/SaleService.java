package com.gestioncomercial.api.sale.application;

import com.gestioncomercial.api.catalog.application.ProductService;
import com.gestioncomercial.api.catalog.domain.Product;
import com.gestioncomercial.api.customer.application.CustomerService;
import com.gestioncomercial.api.customer.domain.Customer;
import com.gestioncomercial.api.inventory.application.InventoryStockService;
import com.gestioncomercial.api.sale.domain.Sale;
import com.gestioncomercial.api.sale.domain.SaleLineInput;
import com.gestioncomercial.api.sale.domain.SaleNotFoundException;
import com.gestioncomercial.api.sale.domain.SalePartyUnavailableException;
import com.gestioncomercial.api.sale.domain.SaleStateConflictException;
import com.gestioncomercial.api.sale.domain.SaleStatus;
import com.gestioncomercial.api.shared.api.InvalidRequestException;
import com.gestioncomercial.api.shared.pagination.PageResult;
import com.gestioncomercial.api.shared.pagination.Pagination;
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
public class SaleService {
    private static final Map<String, String> SORTS = Map.of(
            "id", "id", "customerId", "customer.id", "status", "status",
            "total", "total", "createdAt", "createdAt", "confirmedAt", "confirmedAt");

    private final SaleRepository sales;
    private final CustomerService customers;
    private final ProductService products;
    private final InventoryStockService stock;
    private final Clock clock;

    public SaleService(SaleRepository sales, CustomerService customers, ProductService products,
                       InventoryStockService stock, Clock clock) {
        this.sales = sales;
        this.customers = customers;
        this.products = products;
        this.stock = stock;
        this.clock = clock;
    }

    @Transactional
    public Sale create(CreateSaleCommand command) {
        Customer customer = customers.get(command.customerId());
        if (!customer.isActive()) throw new SalePartyUnavailableException();
        if (command.lines() == null || command.lines().isEmpty() || command.lines().size() > 100) {
            throw new InvalidRequestException("sale must contain between 1 and 100 lines");
        }
        Set<Long> seen = new HashSet<>();
        List<SaleLineInput> inputs = new ArrayList<>();
        for (CreateSaleCommand.Line line : command.lines()) {
            if (!seen.add(line.productId())) {
                throw new InvalidRequestException("each product can appear only once in a sale");
            }
            Product product = products.get(line.productId());
            if (!product.isActive()) throw new SalePartyUnavailableException();
            inputs.add(new SaleLineInput(product, line.quantity()));
        }
        return sales.save(Sale.create(customer, inputs, clock));
    }

    public Sale get(long id) {
        return sales.findById(id).orElseThrow(() -> new SaleNotFoundException(id));
    }

    public PageResult<Sale> list(int page, int size, String sort, SaleStatus status, Long customerId) {
        if (customerId != null && customerId <= 0) {
            throw new InvalidRequestException("customerId must be greater than zero");
        }
        return sales.findAll(new SaleSearchCriteria(status, customerId,
                Pagination.parse(page, size, sort, SORTS)));
    }

    @Transactional
    public Sale confirm(long id) {
        Sale sale = sales.findByIdForUpdate(id).orElseThrow(() -> new SaleNotFoundException(id));
        if (sale.getStatus() != SaleStatus.DRAFT) throw new SaleStateConflictException();
        if (!sale.getCustomer().isActive()) throw new SalePartyUnavailableException();
        for (var line : sale.getLines()) {
            if (!line.getProduct().isActive()) throw new SalePartyUnavailableException();
        }
        sale.getLines().stream()
                .sorted((left, right) -> Long.compare(left.getProduct().getId(), right.getProduct().getId()))
                .forEach(line -> stock.recordSale(line.getProduct().getId(), line.getQuantity(), line.getId()));
        sale.confirm(clock);
        return sales.save(sale);
    }
}
