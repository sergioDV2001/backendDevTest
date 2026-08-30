package com.similarproducts.infrastructure.http.out;

import com.similarproducts.domain.model.ProductDetail;
import com.similarproducts.domain.model.ProductId;
import com.similarproducts.infrastructure.config.CacheProperties;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CachingProductDetailPortTest {

    @Test
    void callsDelegateOnceAndServesCachedValueAfterwards() {
        ProductDetailHttpAdapter delegate = mock(ProductDetailHttpAdapter.class);
        ProductDetail shirt = new ProductDetail(new ProductId("1"), "Shirt", 9.99, true);
        when(delegate.getById(new ProductId("1"))).thenReturn(Mono.just(shirt));
        CachingProductDetailPort caching =
                new CachingProductDetailPort(delegate, new CacheProperties(100, Duration.ofMinutes(1)));

        ProductDetail first = caching.getById(new ProductId("1")).block();
        ProductDetail second = caching.getById(new ProductId("1")).block();

        verify(delegate, times(1)).getById(new ProductId("1"));
        org.junit.jupiter.api.Assertions.assertEquals(shirt, first);
        org.junit.jupiter.api.Assertions.assertEquals(shirt, second);
    }
}
