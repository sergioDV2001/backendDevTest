package com.similarproducts.infrastructure.config;

import com.similarproducts.application.GetSimilarProductsService;
import com.similarproducts.application.GetSimilarProductsUseCase;
import com.similarproducts.domain.port.ProductDetailPort;
import com.similarproducts.domain.port.SimilarProductIdsPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public GetSimilarProductsUseCase getSimilarProductsUseCase(SimilarProductIdsPort similarProductIdsPort,
                                                               ProductDetailPort productDetailPort) {
        return new GetSimilarProductsService(similarProductIdsPort, productDetailPort);
    }
}
