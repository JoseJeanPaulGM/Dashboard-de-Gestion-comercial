package com.gestioncomercial.api.system.api;

import com.gestioncomercial.api.system.application.SystemInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system")
@Tag(name = "System", description = "Technical endpoints available during the backend base phase.")
public class SystemController {

    private final SystemInfoService systemInfoService;

    public SystemController(SystemInfoService systemInfoService) {
        this.systemInfoService = systemInfoService;
    }

    @GetMapping("/info")
    @Operation(summary = "Get application information")
    public SystemInfoResponse information() {
        return systemInfoService.information();
    }

    @PostMapping("/echo")
    @Operation(summary = "Validate and echo a technical request")
    public ResponseEntity<EchoResponse> echo(@Valid @RequestBody EchoRequest request) {
        return ResponseEntity.ok(new EchoResponse(request.message()));
    }
}
