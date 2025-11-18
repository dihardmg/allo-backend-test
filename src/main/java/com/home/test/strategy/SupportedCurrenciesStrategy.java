package com.home.test.strategy;

import com.home.test.dto.CurrencyResponse;
import com.home.test.service.CurrencyMetadataService;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
public class SupportedCurrenciesStrategy implements IDRDataFetcher<CurrencyResponse> {

    private static final String RESOURCE_TYPE = "supported_currencies";
    private final WebClient webClient;
    private final CurrencyMetadataService currencyMetadataService;

    public SupportedCurrenciesStrategy(WebClient webClient, CurrencyMetadataService currencyMetadataService) {
        this.webClient = webClient;
        this.currencyMetadataService = currencyMetadataService;
    }

    @Override
    public Mono<CurrencyResponse> fetchData() {
        return webClient.get()
                .uri("/currencies")
                .retrieve()
                .bodyToMono(Map.class)
                .map(this::transformResponse);
    }

    @Override
    public String getResourceType() {
        return RESOURCE_TYPE;
    }

    private CurrencyResponse transformResponse(Map<String, Object> currenciesMap) {
        List<String> currencies = currenciesMap.keySet().stream().toList();
        // Get enriched currencies from metadata service
        List<String> sortedCurrencies = currencyMetadataService.getSupportedCurrencyCodes();
        return new CurrencyResponse(sortedCurrencies);
    }
}