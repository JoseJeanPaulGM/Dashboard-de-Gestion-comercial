package com.gestioncomercial.api.customer.api;

import com.gestioncomercial.api.customer.application.CustomerService;
import com.gestioncomercial.api.customer.domain.Customer;
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
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    @PostMapping
    ResponseEntity<CustomerResponse> create(@Valid @RequestBody CreateCustomerRequest request) {
        Customer customer = service.create(request.toCommand());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(customer.getId())
                .toUri();
        return ResponseEntity.created(location).body(CustomerResponse.from(customer));
    }

    @GetMapping("/{id}")
    CustomerResponse get(@PathVariable long id) {
        return CustomerResponse.from(service.get(id));
    }

    @GetMapping
    PageResponse<CustomerResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name,asc") String sort,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search
    ) {
        PageResult<Customer> result = service.list(page, size, sort, active, search);
        return PageResponse.from(result, CustomerResponse::from);
    }

    @PutMapping("/{id}")
    CustomerResponse update(
            @PathVariable long id,
            @Valid @RequestBody UpdateCustomerRequest request
    ) {
        return CustomerResponse.from(service.update(id, request.toCommand()));
    }

    @PatchMapping("/{id}/status")
    CustomerResponse changeStatus(
            @PathVariable long id,
            @Valid @RequestBody UpdateCustomerStatusRequest request
    ) {
        return CustomerResponse.from(service.changeStatus(id, request.active()));
    }
}
