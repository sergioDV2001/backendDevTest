package com.similarproducts.infrastructure.http.out;

import com.similarproducts.domain.model.ProductDetail;
import com.similarproducts.domain.model.ProductId;
import com.similarproducts.domain.port.ProductDetailPort;
import com.similarproducts.infrastructure.config.UpstreamProperties;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ProductDetailHttpAdapter implements ProductDetailPort {

    private final WebClient webClient;
    private final UpstreamProperties properties;
    private final CircuitBreaker circuitBreaker;

    public ProductDetailHttpAdapter(WebClient upstreamWebClient,
                                    UpstreamProperties properties,
                                    CircuitBreaker upstreamCircuitBreaker) {
        this.webClient = upstreamWebClient;
        this.properties = properties;
        this.circuitBreaker = upstreamCircuitBreaker;
    }

    @Override
    public Mono<ProductDetail> getById(ProductId productId) {
        return webClient.get()
                .uri("/product/{id}", productId.value())
                .retrieve()
                .bodyToMono(ProductApiResponse.class)
                .timeout(properties.timeout())
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .map(response -> new ProductDetail(
                        new ProductId(response.id()),
                        response.name(),
                        response.price(),
                        response.availability()));
    }
}
