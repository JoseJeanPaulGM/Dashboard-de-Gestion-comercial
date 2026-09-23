package com.gestioncomercial.api.catalog.application;

import com.gestioncomercial.api.catalog.domain.Category;
import com.gestioncomercial.api.catalog.domain.CategoryNotFoundException;
import com.gestioncomercial.api.catalog.domain.Product;
import com.gestioncomercial.api.catalog.domain.ProductCategoryInactiveException;
import com.gestioncomercial.api.catalog.domain.ProductNotFoundException;
import com.gestioncomercial.api.catalog.domain.ProductSkuConflictException;
import com.gestioncomercial.api.shared.api.InvalidRequestException;
import com.gestioncomercial.api.shared.pagination.PageCriteria;
import com.gestioncomercial.api.shared.pagination.PageResult;
import com.gestioncomercial.api.shared.pagination.Pagination;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private static final Map<String, String> SORT_PROPERTIES = Map.of(
            "id", "id",
            "sku", "sku",
            "name", "name",
            "salePrice", "salePrice",
            "categoryName", "category.name",
            "active", "active",
            "createdAt", "createdAt",
            "updatedAt", "updatedAt"
    );

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductDeactivationGuard deactivationGuard;
    private final Clock clock;

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            ProductDeactivationGuard deactivationGuard,
            Clock clock
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.deactivationGuard = deactivationGuard;
        this.clock = clock;
    }

    @Transactional
    public Product create(CreateProductCommand command) {
        String sku = Product.normalizeSku(command.sku());
        ensureSkuAvailable(sku);
        Category category = activeCategory(command.categoryId());
        return productRepository.save(Product.create(
                sku,
                command.name(),
                command.description(),
                command.salePrice(),
                category,
                clock
        ));
    }

    public Product get(long id) {
        return productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }

    public PageResult<Product> list(
            int page,
            int size,
            String sort,
            Boolean active,
            Long categoryId,
            String search,
            BigDecimal minPrice,
            BigDecimal maxPrice
    ) {
        PageCriteria pageCriteria = Pagination.parse(page, size, sort, SORT_PROPERTIES);
        validatePriceRange(minPrice, maxPrice);
        return productRepository.findAll(new ProductSearchCriteria(
                active,
                categoryId,
                normalizeSearch(search),
                minPrice,
                maxPrice,
                pageCriteria
        ));
    }

    @Transactional
    public Product update(long id, UpdateProductCommand command) {
        Product product = get(id);
        String sku = Product.normalizeSku(command.sku());
        if (productRepository.existsBySkuExcludingId(sku, id)) {
            throw new ProductSkuConflictException();
        }

        Category category = category(command.categoryId());
        boolean categoryChanged = !product.getCategory().getId().equals(category.getId());
        if (!category.isActive() && (product.isActive() || categoryChanged)) {
            throw new ProductCategoryInactiveException();
        }

        product.update(
                sku,
                command.name(),
                command.description(),
                command.salePrice(),
                category,
                clock
        );
        return productRepository.save(product);
    }

    @Transactional
    public Product changeStatus(long id, boolean active) {
        Product product = get(id);
        if (active && !product.getCategory().isActive()) {
            throw new ProductCategoryInactiveException();
        }
        if (!active && product.isActive()) {
            deactivationGuard.ensureCanDeactivate(id);
        }
        product.changeActive(active, clock);
        return productRepository.save(product);
    }

    private Category activeCategory(long id) {
        Category category = category(id);
        if (!category.isActive()) {
            throw new ProductCategoryInactiveException();
        }
        return category;
    }

    private Category category(long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new CategoryNotFoundException(id));
    }

    private void ensureSkuAvailable(String sku) {
        if (productRepository.existsBySku(sku)) {
            throw new ProductSkuConflictException();
        }
    }

    private void validatePriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        validatePriceFilter("minPrice", minPrice);
        validatePriceFilter("maxPrice", maxPrice);
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new InvalidRequestException("minPrice must be less than or equal to maxPrice");
        }
    }

    private void validatePriceFilter(String name, BigDecimal value) {
        if (value != null && (value.signum() < 0 || value.stripTrailingZeros().scale() > 2)) {
            throw new InvalidRequestException(name + " must be a non-negative amount with at most 2 decimal places");
        }
    }

    private String normalizeSearch(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        String normalized = search.trim();
        if (normalized.length() > 150) {
            throw new InvalidRequestException("search must not exceed 150 characters");
        }
        return normalized;
    }
}
