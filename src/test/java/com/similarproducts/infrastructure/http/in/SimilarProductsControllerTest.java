package com.similarproducts.infrastructure.http.in;

import com.similarproducts.application.GetSimilarProductsUseCase;
import com.similarproducts.domain.model.ProductDetail;
import com.similarproducts.domain.model.ProductId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import static org.mockito.Mockito.when;

@WebFluxTest(SimilarProductsController.class)
class SimilarProductsControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private GetSimilarProductsUseCase getSimilarProductsUseCase;

    @Test
    void returnsSimilarProductsAsJson() {
        when(getSimilarProductsUseCase.getSimilarProducts(new ProductId("1")))
                .thenReturn(Flux.just(
                        new ProductDetail(new ProductId("2"), "Dress", 19.99, true),
                        new ProductDetail(new ProductId("3"), "Blazer", 29.99, false)));

        webTestClient.get().uri("/product/1/similar")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo("2")
                .jsonPath("$[0].name").isEqualTo("Dress")
                .jsonPath("$[0].price").isEqualTo(19.99)
                .jsonPath("$[0].availability").isEqualTo(true)
                .jsonPath("$[1].id").isEqualTo("3");
    }
}
