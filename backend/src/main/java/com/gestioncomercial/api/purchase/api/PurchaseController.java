package com.gestioncomercial.api.purchase.api;

import com.gestioncomercial.api.purchase.application.PurchaseService;
import com.gestioncomercial.api.purchase.domain.PurchaseStatus;
import com.gestioncomercial.api.shared.api.PageResponse;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/purchases")
public class PurchaseController {
    private final PurchaseService service;

    public PurchaseController(PurchaseService service) {
        this.service = service;
    }

    @PostMapping
    ResponseEntity<PurchaseResponse> create(@Valid @RequestBody CreatePurchaseRequest request) {
        var purchase = service.create(request.toCommand());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(purchase.getId()).toUri();
        return ResponseEntity.created(location).body(PurchaseResponse.from(purchase));
    }

    @GetMapping("/{id}")
    PurchaseResponse get(@PathVariable long id) {
        return PurchaseResponse.from(service.get(id));
    }

    @GetMapping
    PageResponse<PurchaseSummaryResponse> list(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size,
                                               @RequestParam(defaultValue = "createdAt,desc") String sort,
                                               @RequestParam(required = false) PurchaseStatus status,
                                               @RequestParam(required = false) Long supplierId) {
        return PageResponse.from(service.list(page, size, sort, status, supplierId), PurchaseSummaryResponse::from);
    }

    @PostMapping("/{id}/confirm")
    PurchaseResponse confirm(@PathVariable long id) {
        return PurchaseResponse.from(service.confirm(id));
    }
}
