package com.gestioncomercial.api.catalog.api;

import com.gestioncomercial.api.catalog.application.CategoryService;
import com.gestioncomercial.api.catalog.domain.Category;
import com.gestioncomercial.api.shared.api.PageResponse;
import com.gestioncomercial.api.shared.pagination.PageResult;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @PostMapping
    ResponseEntity<CategoryResponse> create(@Valid @RequestBody CreateCategoryRequest request) {
        Category category = service.create(request.toCommand());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(category.getId())
                .toUri();
        return ResponseEntity.created(location).body(CategoryResponse.from(category));
    }

    @GetMapping("/{id}")
    CategoryResponse get(@PathVariable long id) {
        return CategoryResponse.from(service.get(id));
    }

    @GetMapping
    PageResponse<CategoryResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name,asc") String sort,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search
    ) {
        PageResult<Category> result = service.list(page, size, sort, active, search);
        return PageResponse.from(result, CategoryResponse::from);
    }

    @PutMapping("/{id}")
    CategoryResponse update(
            @PathVariable long id,
            @Valid @RequestBody UpdateCategoryRequest request
    ) {
        return CategoryResponse.from(service.update(id, request.toCommand()));
    }

    @PatchMapping("/{id}/status")
    CategoryResponse changeStatus(
            @PathVariable long id,
            @Valid @RequestBody UpdateCatalogStatusRequest request
    ) {
        return CategoryResponse.from(service.changeStatus(id, request.active()));
    }
}
