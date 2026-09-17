package com.gestioncomercial.api.system.api;

import java.time.Instant;

public record SystemInfoResponse(String name, String version, Instant timestamp) {
}
