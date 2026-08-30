package com.similarproducts.infrastructure.http.in;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class SimilarProductsController {

    @GetMapping("/product/{productId}/similar")
    public Flux<ProductDetailResponse> getSimilarProducts(@PathVariable String productId) {
        return Flux.empty();
    }
}
