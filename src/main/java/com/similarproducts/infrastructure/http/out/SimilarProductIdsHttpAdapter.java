package com.similarproducts.infrastructure.http.out;

import com.similarproducts.domain.model.ProductId;
import com.similarproducts.domain.port.SimilarProductIdsPort;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Component
public class SimilarProductIdsHttpAdapter implements SimilarProductIdsPort {

    private final WebClient webClient;

    public SimilarProductIdsHttpAdapter(WebClient upstreamWebClient) {
        this.webClient = upstreamWebClient;
    }

    @Override
    public Flux<ProductId> getSimilarIds(ProductId productId) {
        return webClient.get()
                .uri("/product/{id}/similarids", productId.value())
                .retrieve()
                .bodyToMono(String[].class)
                .flatMapMany(Flux::fromArray)
                .map(ProductId::new);
    }
}
