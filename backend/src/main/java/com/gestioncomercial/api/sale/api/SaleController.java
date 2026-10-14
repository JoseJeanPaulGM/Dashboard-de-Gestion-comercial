package com.gestioncomercial.api.sale.api;

import com.gestioncomercial.api.sale.application.SaleService;
import com.gestioncomercial.api.sale.domain.SaleStatus;
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
@RequestMapping("/api/v1/sales")
public class SaleController {
    private final SaleService service;

    public SaleController(SaleService service) {
        this.service = service;
    }

    @PostMapping
    ResponseEntity<SaleResponse> create(@Valid @RequestBody CreateSaleRequest request) {
        var sale = service.create(request.toCommand());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(sale.getId()).toUri();
        return ResponseEntity.created(location).body(SaleResponse.from(sale));
    }

    @GetMapping("/{id}")
    SaleResponse get(@PathVariable long id) {
        return SaleResponse.from(service.get(id));
    }

    @GetMapping
    PageResponse<SaleSummaryResponse> list(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size,
                                               @RequestParam(defaultValue = "createdAt,desc") String sort,
                                               @RequestParam(required = false) SaleStatus status,
                                               @RequestParam(required = false) Long customerId) {
        return PageResponse.from(service.list(page, size, sort, status, customerId), SaleSummaryResponse::from);
    }

    @PostMapping("/{id}/confirm")
    SaleResponse confirm(@PathVariable long id) {
        return SaleResponse.from(service.confirm(id));
    }
}
