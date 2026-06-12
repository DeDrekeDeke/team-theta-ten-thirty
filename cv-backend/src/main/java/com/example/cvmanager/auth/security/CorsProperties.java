package com.example.cvmanager.auth.security;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(List<String> allowedOrigins) {

    public CorsProperties {
        allowedOrigins = allowedOrigins == null || allowedOrigins.isEmpty()
                ? List.of("https://dedrekedeke.github.io", "http://localhost:5173", "http://127.0.0.1:5173")
                : List.copyOf(allowedOrigins);
    }
}
