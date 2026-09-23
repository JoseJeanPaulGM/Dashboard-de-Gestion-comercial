package com.gestioncomercial.api.supplier.api;

import com.gestioncomercial.api.shared.api.PageResponse;
import com.gestioncomercial.api.shared.pagination.PageResult;
import com.gestioncomercial.api.supplier.application.SupplierService;
import com.gestioncomercial.api.supplier.domain.Supplier;
import com.gestioncomercial.api.supplier.domain.SupplierProduct;
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
@RequestMapping("/api/v1/suppliers")
public class SupplierController {
    private final SupplierService service;

    public SupplierController(SupplierService service) { this.service = service; }

    @PostMapping
    ResponseEntity<SupplierResponse> create(@Valid @RequestBody CreateSupplierRequest request) {
        Supplier supplier = service.create(request.toCommand());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(supplier.getId()).toUri();
        return ResponseEntity.created(location).body(SupplierResponse.from(supplier));
    }

    @GetMapping("/{id}")
    SupplierResponse get(@PathVariable long id) { return SupplierResponse.from(service.get(id)); }

    @GetMapping
    PageResponse<SupplierResponse> list(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size,
                                        @RequestParam(defaultValue = "businessName,asc") String sort,
                                        @RequestParam(required = false) Boolean active,
                                        @RequestParam(required = false) String search) {
        PageResult<Supplier> result = service.list(page, size, sort, active, search);
        return PageResponse.from(result, SupplierResponse::from);
    }

    @PutMapping("/{id}")
    SupplierResponse update(@PathVariable long id, @Valid @RequestBody UpdateSupplierRequest request) {
        return SupplierResponse.from(service.update(id, request.toCommand()));
    }

    @PatchMapping("/{id}/status")
    SupplierResponse changeStatus(@PathVariable long id, @Valid @RequestBody UpdateSupplierStatusRequest request) {
        return SupplierResponse.from(service.changeStatus(id, request.active()));
    }

    @PostMapping("/{supplierId}/products")
    ResponseEntity<SupplierProductResponse> linkProduct(@PathVariable long supplierId,
                                                         @Valid @RequestBody LinkSupplierProductRequest request) {
        SupplierProduct link = service.linkProduct(supplierId, request.productId(), request.supplierProductCode());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{productId}")
                .buildAndExpand(request.productId()).toUri();
        return ResponseEntity.created(location).body(SupplierProductResponse.from(link));
    }

    @GetMapping("/{supplierId}/products")
    PageResponse<SupplierProductResponse> listProducts(@PathVariable long supplierId,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "20") int size,
                                                        @RequestParam(defaultValue = "name,asc") String sort,
                                                        @RequestParam(required = false) Boolean active,
                                                        @RequestParam(required = false) String search) {
        return PageResponse.from(service.listProducts(supplierId, page, size, sort, active, search),
                SupplierProductResponse::from);
    }

    @PutMapping("/{supplierId}/products/{productId}")
    SupplierProductResponse updateProduct(@PathVariable long supplierId, @PathVariable long productId,
                                          @Valid @RequestBody UpdateSupplierProductRequest request) {
        return SupplierProductResponse.from(service.updateProductCode(supplierId, productId,
                request.supplierProductCode()));
    }

    @PatchMapping("/{supplierId}/products/{productId}/status")
    SupplierProductResponse changeProductStatus(@PathVariable long supplierId, @PathVariable long productId,
                                                 @Valid @RequestBody UpdateSupplierStatusRequest request) {
        return SupplierProductResponse.from(service.changeProductStatus(supplierId, productId, request.active()));
    }
}
