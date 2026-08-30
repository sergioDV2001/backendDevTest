package com.similarproducts.application;

import com.similarproducts.domain.model.ProductDetail;
import com.similarproducts.domain.model.ProductId;
import com.similarproducts.domain.port.ProductDetailPort;
import com.similarproducts.domain.port.SimilarProductIdsPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class GetSimilarProductsService implements GetSimilarProductsUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetSimilarProductsService.class);

    private static final int MAX_CONCURRENT_DETAIL_REQUESTS = 50;

    private final SimilarProductIdsPort similarProductIdsPort;
    private final ProductDetailPort productDetailPort;

    public GetSimilarProductsService(SimilarProductIdsPort similarProductIdsPort, ProductDetailPort productDetailPort) {
        this.similarProductIdsPort = similarProductIdsPort;
        this.productDetailPort = productDetailPort;
    }

    @Override
    public Flux<ProductDetail> getSimilarProducts(ProductId productId) {
        return similarProductIdsPort.getSimilarIds(productId)
                .flatMapSequential(similarId -> productDetailPort.getById(similarId)
                        .onErrorResume(error -> skip(similarId, error)), MAX_CONCURRENT_DETAIL_REQUESTS);
    }

    private Mono<ProductDetail> skip(ProductId similarId, Throwable error) {
        log.warn("Skipping similar product {}: {}", similarId.value(), error.toString());
        return Mono.empty();
    }
}
