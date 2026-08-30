package com.similarproducts.application;

import com.similarproducts.domain.model.ProductDetail;
import com.similarproducts.domain.model.ProductId;
import com.similarproducts.domain.port.ProductDetailPort;
import com.similarproducts.domain.port.SimilarProductIdsPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetSimilarProductsServiceTest {

    private SimilarProductIdsPort similarProductIdsPort;
    private ProductDetailPort productDetailPort;
    private GetSimilarProductsService service;

    @BeforeEach
    void setUp() {
        similarProductIdsPort = mock(SimilarProductIdsPort.class);
        productDetailPort = mock(ProductDetailPort.class);
        service = new GetSimilarProductsService(similarProductIdsPort, productDetailPort);
    }

    @Test
    void returnsSimilarProductDetailsPreservingSimilarityOrder() {
        ProductId productId = new ProductId("1");
        ProductDetail dress = new ProductDetail(new ProductId("2"), "Dress", 19.99, true);
        ProductDetail blazer = new ProductDetail(new ProductId("3"), "Blazer", 29.99, false);
        when(similarProductIdsPort.getSimilarIds(productId))
                .thenReturn(Flux.just(new ProductId("2"), new ProductId("3")));
        when(productDetailPort.getById(new ProductId("2"))).thenReturn(Mono.just(dress));
        when(productDetailPort.getById(new ProductId("3"))).thenReturn(Mono.just(blazer));

        StepVerifier.create(service.getSimilarProducts(productId))
                .expectNext(dress)
                .expectNext(blazer)
                .verifyComplete();
    }

    @Test
    void returnsEmptyWhenProductHasNoSimilars() {
        ProductId productId = new ProductId("1");
        when(similarProductIdsPort.getSimilarIds(productId)).thenReturn(Flux.empty());

        StepVerifier.create(service.getSimilarProducts(productId))
                .verifyComplete();
    }
}
