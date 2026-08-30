package com.similarproducts.infrastructure.http.out;

import com.similarproducts.domain.model.ProductId;
import com.similarproducts.domain.model.ProductNotFoundException;
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

class SimilarProductIdsHttpAdapterTest {

    private MockWebServer server;
    private SimilarProductIdsHttpAdapter adapter;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        WebClient webClient = WebClient.builder().baseUrl(server.url("/").toString()).build();
        UpstreamProperties properties = new UpstreamProperties(server.url("/").toString(), Duration.ofSeconds(2));
        adapter = new SimilarProductIdsHttpAdapter(webClient, properties, CircuitBreaker.ofDefaults("test"));
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void parsesTheJsonArrayIntoProductIds() {
        server.enqueue(new MockResponse()
                .setBody("[2,3,4]")
                .addHeader("Content-Type", "application/json"));

        StepVerifier.create(adapter.getSimilarIds(new ProductId("1")))
                .expectNext(new ProductId("2"), new ProductId("3"), new ProductId("4"))
                .verifyComplete();
    }

    @Test
    void mapsNotFoundToProductNotFoundException() {
        server.enqueue(new MockResponse().setResponseCode(404));

        StepVerifier.create(adapter.getSimilarIds(new ProductId("6")))
                .expectError(ProductNotFoundException.class)
                .verify();
    }
}
