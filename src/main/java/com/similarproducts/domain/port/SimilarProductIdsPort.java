package com.similarproducts.domain.port;

import com.similarproducts.domain.model.ProductId;
import reactor.core.publisher.Flux;

public interface SimilarProductIdsPort {

    Flux<ProductId> getSimilarIds(ProductId productId);
}
