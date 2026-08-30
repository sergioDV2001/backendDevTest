package com.similarproducts.infrastructure.http.in;

import com.similarproducts.application.GetSimilarProductsUseCase;
import com.similarproducts.domain.model.ProductId;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class SimilarProductsController {

    private final GetSimilarProductsUseCase getSimilarProductsUseCase;

    public SimilarProductsController(GetSimilarProductsUseCase getSimilarProductsUseCase) {
        this.getSimilarProductsUseCase = getSimilarProductsUseCase;
    }

    @GetMapping("/product/{productId}/similar")
    public Flux<ProductDetailResponse> getSimilarProducts(@PathVariable String productId) {
        return getSimilarProductsUseCase.getSimilarProducts(new ProductId(productId))
                .map(product -> new ProductDetailResponse(
                        product.id().value(),
                        product.name(),
                        product.price(),
                        product.availability()));
    }
}
