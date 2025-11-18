package com.home.test.strategy;

import com.home.test.dto.CurrencyResponse;
import com.home.test.service.CurrencyMetadataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class SupportedCurrenciesStrategyTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private CurrencyMetadataService currencyMetadataService;

    private SupportedCurrenciesStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);

        // Mock currency metadata service to return sorted currency codes
        List<String> mockCurrencies = List.of("AUD", "BGN", "BRL", "CAD", "CHF", "CNY", "CZK",
                                            "DKK", "EUR", "GBP", "HKD", "HUF", "IDR", "ILS",
                                            "INR", "ISK", "JPY", "KRW", "MXN", "MYR", "NOK",
                                            "NZD", "PHP", "PLN", "RON", "SEK", "SGD", "THB",
                                            "TRY", "USD", "ZAR");
        when(currencyMetadataService.getSupportedCurrencyCodes()).thenReturn(mockCurrencies);

        strategy = new SupportedCurrenciesStrategy(webClient, currencyMetadataService);
    }

    @Test
    void testFetchData_Success() {
        Map<String, Object> mockCurrenciesMap = Map.of(
                "USD", "United States Dollar",
                "EUR", "Euro",
                "IDR", "Indonesian Rupiah",
                "JPY", "Japanese Yen"
        );

        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(mockCurrenciesMap));

        Mono<CurrencyResponse> result = strategy.fetchData();

        StepVerifier.create(result)
                .assertNext(response -> {
                    // Now returns all supported currencies from metadata service, not just from API response
                    assertEquals(31, response.getCurrencies().size()); // All currencies from metadata service
                    assertTrue(response.getCurrencies().contains("USD"));
                    assertTrue(response.getCurrencies().contains("EUR"));
                    assertTrue(response.getCurrencies().contains("IDR"));
                    assertTrue(response.getCurrencies().contains("JPY"));
                    assertTrue(response.getCurrencies().contains("AUD"));
                    assertTrue(response.getCurrencies().contains("BGN"));
                })
                .verifyComplete();

        verify(webClient).get();
        verify(requestHeadersUriSpec).uri("/currencies");
        verify(responseSpec).bodyToMono(Map.class);
        verify(currencyMetadataService).getSupportedCurrencyCodes();
    }

    @Test
    void testFetchData_ApiError() {
        WebClientResponseException exception = WebClientResponseException.create(500, "Internal Server Error", null, null, null);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.error(exception));

        Mono<CurrencyResponse> result = strategy.fetchData();

        StepVerifier.create(result)
                .expectError(WebClientResponseException.class)
                .verify();

        verify(webClient).get();
        verify(requestHeadersUriSpec).uri("/currencies");
        verify(responseSpec).bodyToMono(Map.class);
    }

    @Test
    void testFetchData_EmptyResponse() {
        Map<String, Object> emptyMap = Map.of();
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(emptyMap));

        Mono<CurrencyResponse> result = strategy.fetchData();

        StepVerifier.create(result)
                .assertNext(response -> {
                    // Still returns all supported currencies from metadata service even if API returns empty
                    assertEquals(31, response.getCurrencies().size());
                    assertTrue(response.getCurrencies().contains("USD"));
                    assertTrue(response.getCurrencies().contains("EUR"));
                })
                .verifyComplete();

        verify(webClient).get();
        verify(requestHeadersUriSpec).uri("/currencies");
        verify(responseSpec).bodyToMono(Map.class);
        verify(currencyMetadataService).getSupportedCurrencyCodes();
    }

    @Test
    void testGetResourceType() {
        assertEquals("supported_currencies", strategy.getResourceType());
    }
}