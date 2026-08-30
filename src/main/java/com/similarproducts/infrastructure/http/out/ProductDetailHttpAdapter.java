package com.similarproducts.infrastructure.http.out;

import com.similarproducts.domain.model.ProductDetail;
import com.similarproducts.domain.model.ProductId;
import com.similarproducts.domain.port.ProductDetailPort;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ProductDetailHttpAdapter implements ProductDetailPort {

    private final WebClient webClient;

    public ProductDetailHttpAdapter(WebClient upstreamWebClient) {
        this.webClient = upstreamWebClient;
    }

    @Override
    public Mono<ProductDetail> getById(ProductId productId) {
        return webClient.get()
                .uri("/product/{id}", productId.value())
                .retrieve()
                .bodyToMono(ProductApiResponse.class)
                .map(response -> new ProductDetail(
                        new ProductId(response.id()),
                        response.name(),
                        response.price(),
                        response.availability()));
    }
}
