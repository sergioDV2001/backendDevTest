package com.similarproducts.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "cache")
public record CacheProperties(long maximumSize, Duration expireAfterWrite) {
}
