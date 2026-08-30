package com.similarproducts.infrastructure.http.out;

public record ProductApiResponse(String id, String name, double price, boolean availability) {
}
