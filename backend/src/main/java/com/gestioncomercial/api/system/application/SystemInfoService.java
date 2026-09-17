package com.gestioncomercial.api.system.application;

import com.gestioncomercial.api.shared.config.ApplicationProperties;
import com.gestioncomercial.api.system.api.SystemInfoResponse;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class SystemInfoService {

    private final ApplicationProperties applicationProperties;
    private final Clock clock;

    public SystemInfoService(ApplicationProperties applicationProperties, Clock clock) {
        this.applicationProperties = applicationProperties;
        this.clock = clock;
    }

    public SystemInfoResponse information() {
        return new SystemInfoResponse(
                applicationProperties.name(),
                applicationProperties.version(),
                Instant.now(clock)
        );
    }
}
