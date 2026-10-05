package com.example.helloxldxlr;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ApplicationController {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${app.version}")
    private String version;

    @Value("${app.environment:LOCAL}")
    private String environment;

    @GetMapping("/")
    public Map<String, String> home() {
        return Map.of(
                "application", applicationName,
                "version", version,
                "environment", environment,
                "message", "Hello from XLD/XLR!"
        );
    }

    @GetMapping("/api/info")
    public Map<String, String> info() {
        return Map.of(
                "application", applicationName,
                "version", version,
                "environment", environment
        );
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
                "status", "UP",
                "environment", environment
        );
    }

}