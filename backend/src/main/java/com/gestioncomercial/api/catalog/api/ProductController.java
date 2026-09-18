package com.gestioncomercial.api.catalog.api;

import com.gestioncomercial.api.catalog.application.ProductService;
import com.gestioncomercial.api.catalog.domain.Product;
import com.gestioncomercial.api.shared.api.PageResponse;
import com.gestioncomercial.api.shared.pagination.PageResult;
import jakarta.validation.Valid;
import java.math.BigDecimal;
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
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @PostMapping
    ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        Product product = service.create(request.toCommand());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(product.getId())
                .toUri();
        return ResponseEntity.created(location).body(ProductResponse.from(product));
    }

    @GetMapping("/{id}")
    ProductResponse get(@PathVariable long id) {
        return ProductResponse.from(service.get(id));
    }

    @GetMapping
    PageResponse<ProductResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name,asc") String sort,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice
    ) {
        PageResult<Product> result = service.list(
                page,
                size,
                sort,
                active,
                categoryId,
                search,
                minPrice,
                maxPrice
        );
        return PageResponse.from(result, ProductResponse::from);
    }

    @PutMapping("/{id}")
    ProductResponse update(
            @PathVariable long id,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        return ProductResponse.from(service.update(id, request.toCommand()));
    }

    @PatchMapping("/{id}/status")
    ProductResponse changeStatus(
            @PathVariable long id,
            @Valid @RequestBody UpdateCatalogStatusRequest request
    ) {
        return ProductResponse.from(service.changeStatus(id, request.active()));
    }
}
