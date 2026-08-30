package com.similarproducts.application;

import com.similarproducts.domain.model.ProductDetail;
import com.similarproducts.domain.model.ProductId;
import reactor.core.publisher.Flux;

public interface GetSimilarProductsUseCase {

    Flux<ProductDetail> getSimilarProducts(ProductId productId);
}
