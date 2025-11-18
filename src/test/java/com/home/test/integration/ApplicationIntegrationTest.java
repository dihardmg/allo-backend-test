package com.home.test.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ApplicationIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testHealthEndpoint() {
        webTestClient.get()
                .uri("/api/finance/data/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").exists()
                .jsonPath("$.initialized").exists();
    }

    @Test
    void testInvalidResourceType() {
        webTestClient.get()
                .uri("/api/finance/data/invalid_type")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("Invalid Resource Type")
                .jsonPath("$.message").exists();
    }

    @Test
    void testServiceInitializedSuccessfully() {
        webTestClient.get()
                .uri("/api/finance/data/latest_idr_rates")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.base").isEqualTo("IDR")
                .jsonPath("$.rates").exists()
                .jsonPath("$.rates.USD").exists()
                .jsonPath("$.USD_BuySpread_IDR").exists();
    }
}
