package com.gestioncomercial.api.catalog.application;

import com.gestioncomercial.api.catalog.domain.Category;
import com.gestioncomercial.api.catalog.domain.CategoryCodeConflictException;
import com.gestioncomercial.api.catalog.domain.CategoryHasActiveProductsException;
import com.gestioncomercial.api.catalog.domain.CategoryNotFoundException;
import com.gestioncomercial.api.shared.api.InvalidRequestException;
import com.gestioncomercial.api.shared.pagination.PageCriteria;
import com.gestioncomercial.api.shared.pagination.PageResult;
import com.gestioncomercial.api.shared.pagination.Pagination;
import java.time.Clock;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private static final Map<String, String> SORT_PROPERTIES = Map.of(
            "id", "id",
            "code", "code",
            "name", "name",
            "active", "active",
            "createdAt", "createdAt",
            "updatedAt", "updatedAt"
    );

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final Clock clock;

    public CategoryService(
            CategoryRepository categoryRepository,
            ProductRepository productRepository,
            Clock clock
    ) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.clock = clock;
    }

    @Transactional
    public Category create(CreateCategoryCommand command) {
        String code = Category.normalizeCode(command.code());
        ensureCodeAvailable(code);
        return categoryRepository.save(Category.create(code, command.name(), command.description(), clock));
    }

    public Category get(long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new CategoryNotFoundException(id));
    }

    public PageResult<Category> list(int page, int size, String sort, Boolean active, String search) {
        PageCriteria pageCriteria = Pagination.parse(page, size, sort, SORT_PROPERTIES);
        return categoryRepository.findAll(new CategorySearchCriteria(active, normalizeSearch(search), pageCriteria));
    }

    @Transactional
    public Category update(long id, UpdateCategoryCommand command) {
        Category category = get(id);
        String code = Category.normalizeCode(command.code());
        if (categoryRepository.existsByCodeExcludingId(code, id)) {
            throw new CategoryCodeConflictException();
        }
        category.update(code, command.name(), command.description(), clock);
        return categoryRepository.save(category);
    }

    @Transactional
    public Category changeStatus(long id, boolean active) {
        Category category = get(id);
        if (!active && category.isActive() && productRepository.existsActiveByCategoryId(id)) {
            throw new CategoryHasActiveProductsException();
        }
        category.changeActive(active, clock);
        return categoryRepository.save(category);
    }

    private void ensureCodeAvailable(String code) {
        if (categoryRepository.existsByCode(code)) {
            throw new CategoryCodeConflictException();
        }
    }

    private String normalizeSearch(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        String normalized = search.trim();
        if (normalized.length() > 100) {
            throw new InvalidRequestException("search must not exceed 100 characters");
        }
        return normalized;
    }
}
