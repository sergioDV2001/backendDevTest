package com.similarproducts.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "upstream")
public record UpstreamProperties(String baseUrl, Duration timeout) {
}
