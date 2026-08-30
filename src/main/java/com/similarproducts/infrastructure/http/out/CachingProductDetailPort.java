package com.similarproducts.infrastructure.http.out;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.similarproducts.domain.model.ProductDetail;
import com.similarproducts.domain.model.ProductId;
import com.similarproducts.domain.port.ProductDetailPort;
import com.similarproducts.infrastructure.config.CacheProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Primary
public class CachingProductDetailPort implements ProductDetailPort {

    private final ProductDetailPort delegate;
    private final AsyncCache<String, ProductDetail> cache;

    public CachingProductDetailPort(ProductDetailHttpAdapter delegate, CacheProperties properties) {
        this.delegate = delegate;
        this.cache = Caffeine.newBuilder()
                .maximumSize(properties.maximumSize())
                .expireAfterWrite(properties.expireAfterWrite())
                .buildAsync();
    }

    @Override
    public Mono<ProductDetail> getById(ProductId productId) {
        return Mono.defer(() -> Mono.fromFuture(
                cache.get(productId.value(), (key, executor) -> delegate.getById(productId).toFuture())));
    }
}
