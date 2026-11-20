package com.openshelf.admin.controller;

import com.openshelf.common.api.ApiResponse;
import org.springframework.boot.health.actuate.endpoint.HealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/system")
public class AdminSystemController {

    private final HealthEndpoint healthEndpoint;
    private final Environment environment;

    public AdminSystemController(HealthEndpoint healthEndpoint, Environment environment) {
        this.healthEndpoint = healthEndpoint;
        this.environment = environment;
    }

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        HealthDescriptor health = healthEndpoint.health()
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("status", health.getStatus().getCode());
        out.put("application", environment.getProperty("spring.application.name", "openshelf-backend"));
        out.put("profiles", Arrays.asList(environment.getActiveProfiles()));
        out.put("timestamp", Instant.now());
        return ApiResponse.ok(out);
    }
}
