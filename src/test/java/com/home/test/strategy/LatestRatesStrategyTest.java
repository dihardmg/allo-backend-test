package com.home.test.strategy;

import com.home.test.config.GithubProperties;
import com.home.test.dto.FrankfurterLatestResponse;
import com.home.test.dto.LatestRatesResponse;
import com.home.test.util.SpreadFactorCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class LatestRatesStrategyTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private GithubProperties githubProperties;

    @Mock
    private SpreadFactorCalculator spreadFactorCalculator;

    private LatestRatesStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);

        strategy = new LatestRatesStrategy(webClient, githubProperties, spreadFactorCalculator);
    }

    @Test
    void testFetchData_Success() {
        String username = "testuser";
        double spreadFactor = 0.005;
        double usdRate = 0.000065;
        double expectedUsdBuySpread = (1.0 / usdRate) * (1.0 + spreadFactor);

        when(githubProperties.getUsername()).thenReturn(username);
        when(spreadFactorCalculator.calculateSpreadFactor(username)).thenReturn(spreadFactor);
        when(spreadFactorCalculator.calculateUSDBuySpreadIdr(usdRate, spreadFactor)).thenReturn(expectedUsdBuySpread);

        FrankfurterLatestResponse mockResponse = new FrankfurterLatestResponse();
        mockResponse.setBase("IDR");
        mockResponse.setDate("2024-01-01");
        mockResponse.setRates(Map.of("USD", usdRate, "EUR", 0.000059));

        when(responseSpec.bodyToMono(FrankfurterLatestResponse.class)).thenReturn(Mono.just(mockResponse));

        Mono<LatestRatesResponse> result = strategy.fetchData();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals("IDR", response.getBase());
                    assertEquals("2024-01-01", response.getDate());
                    assertEquals(usdRate, response.getRates().get("USD"));
                    assertEquals(expectedUsdBuySpread, response.getUsdBuySpreadIdr());
                })
                .verifyComplete();

        verify(webClient).get();
        verify(requestHeadersUriSpec).uri("/latest?base=IDR");
        verify(responseSpec).bodyToMono(FrankfurterLatestResponse.class);
    }

    @Test
    void testFetchData_ApiError() {
        WebClientResponseException exception = WebClientResponseException.create(500, "Internal Server Error", null, null, null);
        when(responseSpec.bodyToMono(FrankfurterLatestResponse.class)).thenReturn(Mono.error(exception));

        Mono<LatestRatesResponse> result = strategy.fetchData();

        StepVerifier.create(result)
                .expectError(WebClientResponseException.class)
                .verify();
    }

    @Test
    void testFetchData_NullUSDRate() {
        String username = "testuser";
        double spreadFactor = 0.005;

        when(githubProperties.getUsername()).thenReturn(username);
        when(spreadFactorCalculator.calculateSpreadFactor(username)).thenReturn(spreadFactor);

        FrankfurterLatestResponse mockResponse = new FrankfurterLatestResponse();
        mockResponse.setBase("IDR");
        mockResponse.setDate("2024-01-01");
        mockResponse.setRates(Map.of("EUR", 0.000059));

        when(responseSpec.bodyToMono(FrankfurterLatestResponse.class)).thenReturn(Mono.just(mockResponse));

        Mono<LatestRatesResponse> result = strategy.fetchData();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals("IDR", response.getBase());
                    assertEquals("2024-01-01", response.getDate());
                    assertNull(response.getUsdBuySpreadIdr());
                })
                .verifyComplete();
    }

    @Test
    void testGetResourceType() {
        assertEquals("latest_idr_rates", strategy.getResourceType());
    }
}