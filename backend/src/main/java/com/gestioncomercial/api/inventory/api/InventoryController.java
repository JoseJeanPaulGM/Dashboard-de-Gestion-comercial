package com.gestioncomercial.api.inventory.api;

import com.gestioncomercial.api.inventory.application.InventoryBalance;
import com.gestioncomercial.api.inventory.application.InventoryMovement;
import com.gestioncomercial.api.inventory.application.InventoryService;
import com.gestioncomercial.api.shared.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {
    private final InventoryService service;

    public InventoryController(InventoryService service) {
        this.service = service;
    }

    @GetMapping("/balances/{productId}")
    InventoryBalance balance(@PathVariable long productId) {
        return service.balance(productId);
    }

    @PutMapping("/balances/{productId}/minimum")
    InventoryBalance setMinimum(@PathVariable long productId, @Valid @RequestBody MinimumRequest request) {
        return service.setMinimum(productId, request.minimumQuantity());
    }

    @GetMapping("/balances/{productId}/movements")
    PageResponse<InventoryMovement> movements(@PathVariable long productId,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size,
                                              @RequestParam(defaultValue = "id,desc") String sort) {
        return PageResponse.from(service.movements(productId, page, size, sort), movement -> movement);
    }

    @GetMapping("/alerts")
    PageResponse<InventoryBalance> alerts(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "20") int size,
                                          @RequestParam(defaultValue = "productId,asc") String sort) {
        return PageResponse.from(service.alerts(page, size, sort), balance -> balance);
    }

    @PostMapping("/adjustments")
    InventoryMovement adjust(@Valid @RequestBody AdjustmentRequest request) {
        return service.adjust(request.productId(), request.quantityChange(), request.reason(), request.requestKey());
    }
}
