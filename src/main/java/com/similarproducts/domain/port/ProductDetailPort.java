package com.similarproducts.domain.port;

import com.similarproducts.domain.model.ProductDetail;
import com.similarproducts.domain.model.ProductId;
import reactor.core.publisher.Mono;

public interface ProductDetailPort {

    Mono<ProductDetail> getById(ProductId productId);
}
