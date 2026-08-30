package com.similarproducts.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "upstream")
public record UpstreamProperties(String baseUrl) {
}
