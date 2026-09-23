package com.gestioncomercial.api.supplier.application;

import com.gestioncomercial.api.catalog.application.ProductService;
import com.gestioncomercial.api.catalog.domain.Product;
import com.gestioncomercial.api.shared.api.InvalidRequestException;
import com.gestioncomercial.api.shared.pagination.PageCriteria;
import com.gestioncomercial.api.shared.pagination.PageResult;
import com.gestioncomercial.api.shared.pagination.Pagination;
import com.gestioncomercial.api.supplier.domain.Supplier;
import com.gestioncomercial.api.supplier.domain.SupplierHasActiveProductsException;
import com.gestioncomercial.api.supplier.domain.SupplierNotFoundException;
import com.gestioncomercial.api.supplier.domain.SupplierProduct;
import com.gestioncomercial.api.supplier.domain.SupplierProductConflictException;
import com.gestioncomercial.api.supplier.domain.SupplierProductInactivePartyException;
import com.gestioncomercial.api.supplier.domain.SupplierProductNotFoundException;
import com.gestioncomercial.api.supplier.domain.SupplierRucConflictException;
import java.time.Clock;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SupplierService {

    private static final Map<String, String> SUPPLIER_SORTS = Map.of(
            "id", "id", "ruc", "ruc", "businessName", "businessName", "tradeName", "tradeName",
            "active", "active", "createdAt", "createdAt", "updatedAt", "updatedAt");
    private static final Map<String, String> PRODUCT_SORTS = Map.of(
            "id", "id", "sku", "product.sku", "name", "product.name",
            "supplierProductCode", "supplierProductCode", "active", "active",
            "createdAt", "createdAt", "updatedAt", "updatedAt");

    private final SupplierRepository supplierRepository;
    private final SupplierProductRepository supplierProductRepository;
    private final ProductService productService;
    private final Clock clock;

    public SupplierService(SupplierRepository supplierRepository,
                           SupplierProductRepository supplierProductRepository,
                           ProductService productService, Clock clock) {
        this.supplierRepository = supplierRepository;
        this.supplierProductRepository = supplierProductRepository;
        this.productService = productService;
        this.clock = clock;
    }

    @Transactional
    public Supplier create(CreateSupplierCommand command) {
        String ruc = Supplier.normalizeRuc(command.ruc());
        if (supplierRepository.existsByRuc(ruc)) throw new SupplierRucConflictException();
        return supplierRepository.save(Supplier.create(ruc, command.businessName(), command.tradeName(),
                command.email(), command.phone(), command.address(), clock));
    }

    public Supplier get(long id) {
        return supplierRepository.findById(id).orElseThrow(() -> new SupplierNotFoundException(id));
    }

    public PageResult<Supplier> list(int page, int size, String sort, Boolean active, String search) {
        PageCriteria criteria = Pagination.parse(page, size, sort, SUPPLIER_SORTS);
        return supplierRepository.findAll(new SupplierSearchCriteria(active, normalizeSearch(search), criteria));
    }

    @Transactional
    public Supplier update(long id, UpdateSupplierCommand command) {
        Supplier supplier = get(id);
        String ruc = Supplier.normalizeRuc(command.ruc());
        if (supplierRepository.existsByRucExcludingId(ruc, id)) throw new SupplierRucConflictException();
        supplier.update(ruc, command.businessName(), command.tradeName(), command.email(), command.phone(),
                command.address(), clock);
        return supplierRepository.save(supplier);
    }

    @Transactional
    public Supplier changeStatus(long id, boolean active) {
        Supplier supplier = get(id);
        if (!active && supplierProductRepository.existsActiveBySupplierId(id)) {
            throw new SupplierHasActiveProductsException();
        }
        supplier.changeActive(active, clock);
        return supplierRepository.save(supplier);
    }

    @Transactional
    public SupplierProduct linkProduct(long supplierId, long productId, String supplierProductCode) {
        Supplier supplier = get(supplierId);
        Product product = productService.get(productId);
        ensureActiveParties(supplier, product);
        if (supplierProductRepository.existsBySupplierIdAndProductId(supplierId, productId)) {
            throw new SupplierProductConflictException();
        }
        return supplierProductRepository.save(SupplierProduct.create(supplier, product, supplierProductCode, clock));
    }

    public PageResult<SupplierProduct> listProducts(long supplierId, int page, int size, String sort,
                                                    Boolean active, String search) {
        get(supplierId);
        PageCriteria criteria = Pagination.parse(page, size, sort, PRODUCT_SORTS);
        return supplierProductRepository.findAll(new SupplierProductSearchCriteria(
                supplierId, active, normalizeSearch(search), criteria));
    }

    @Transactional
    public SupplierProduct updateProductCode(long supplierId, long productId, String supplierProductCode) {
        SupplierProduct link = link(supplierId, productId);
        link.updateCode(supplierProductCode, clock);
        return supplierProductRepository.save(link);
    }

    @Transactional
    public SupplierProduct changeProductStatus(long supplierId, long productId, boolean active) {
        SupplierProduct link = link(supplierId, productId);
        if (active) ensureActiveParties(link.getSupplier(), link.getProduct());
        link.changeActive(active, clock);
        return supplierProductRepository.save(link);
    }

    private SupplierProduct link(long supplierId, long productId) {
        get(supplierId);
        return supplierProductRepository.findBySupplierIdAndProductId(supplierId, productId)
                .orElseThrow(() -> new SupplierProductNotFoundException(supplierId, productId));
    }

    private void ensureActiveParties(Supplier supplier, Product product) {
        if (!supplier.isActive() || !product.isActive()) throw new SupplierProductInactivePartyException();
    }

    private String normalizeSearch(String search) {
        if (search == null || search.isBlank()) return null;
        String normalized = search.trim();
        if (normalized.length() > 150) throw new InvalidRequestException("search must not exceed 150 characters");
        return normalized;
    }
}
