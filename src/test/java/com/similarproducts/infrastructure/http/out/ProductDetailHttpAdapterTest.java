package com.similarproducts.infrastructure.http.out;

import com.similarproducts.domain.model.ProductDetail;
import com.similarproducts.domain.model.ProductId;
import com.similarproducts.infrastructure.config.UpstreamProperties;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;

class ProductDetailHttpAdapterTest {

    private MockWebServer server;
    private ProductDetailHttpAdapter adapter;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        WebClient webClient = WebClient.builder().baseUrl(server.url("/").toString()).build();
        UpstreamProperties properties = new UpstreamProperties(server.url("/").toString(), Duration.ofSeconds(2));
        adapter = new ProductDetailHttpAdapter(webClient, properties, CircuitBreaker.ofDefaults("test"));
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void mapsTheJsonResponseIntoProductDetail() {
        server.enqueue(new MockResponse()
                .setBody("{\"id\":\"1\",\"name\":\"Shirt\",\"price\":9.99,\"availability\":true}")
                .addHeader("Content-Type", "application/json"));

        StepVerifier.create(adapter.getById(new ProductId("1")))
                .expectNext(new ProductDetail(new ProductId("1"), "Shirt", 9.99, true))
                .verifyComplete();
    }

    @Test
    void propagatesErrorWhenUpstreamFails() {
        server.enqueue(new MockResponse().setResponseCode(500));

        StepVerifier.create(adapter.getById(new ProductId("6")))
                .expectError()
                .verify();
    }
}
