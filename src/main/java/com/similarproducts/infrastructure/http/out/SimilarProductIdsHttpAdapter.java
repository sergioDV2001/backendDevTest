package com.similarproducts.infrastructure.http.out;

import com.similarproducts.domain.model.ProductId;
import com.similarproducts.domain.model.ProductNotFoundException;
import com.similarproducts.domain.port.SimilarProductIdsPort;
import com.similarproducts.infrastructure.config.UpstreamProperties;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class SimilarProductIdsHttpAdapter implements SimilarProductIdsPort {

    private final WebClient webClient;
    private final UpstreamProperties properties;
    private final CircuitBreaker circuitBreaker;

    public SimilarProductIdsHttpAdapter(WebClient upstreamWebClient,
                                        UpstreamProperties properties,
                                        CircuitBreaker upstreamCircuitBreaker) {
        this.webClient = upstreamWebClient;
        this.properties = properties;
        this.circuitBreaker = upstreamCircuitBreaker;
    }

    @Override
    public Flux<ProductId> getSimilarIds(ProductId productId) {
        return webClient.get()
                .uri("/product/{id}/similarids", productId.value())
                .retrieve()
                .bodyToMono(String[].class)
                .timeout(properties.timeout())
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .onErrorResume(WebClientResponseException.NotFound.class,
                        error -> Mono.error(new ProductNotFoundException(productId)))
                .flatMapMany(Flux::fromArray)
                .map(ProductId::new);
    }
}
