package com.similarproducts.infrastructure.http.in;

public record ProductDetailResponse(String id, String name, double price, boolean availability) {
}
