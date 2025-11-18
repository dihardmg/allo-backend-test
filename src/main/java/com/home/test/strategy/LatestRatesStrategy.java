package com.home.test.strategy;

import com.home.test.config.GithubProperties;
import com.home.test.dto.FrankfurterLatestResponse;
import com.home.test.dto.LatestRatesResponse;
import com.home.test.util.SpreadFactorCalculator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class LatestRatesStrategy implements IDRDataFetcher<LatestRatesResponse> {

    private static final String RESOURCE_TYPE = "latest_idr_rates";
    private final WebClient webClient;
    private final GithubProperties githubProperties;
    private final SpreadFactorCalculator spreadFactorCalculator;

    public LatestRatesStrategy(WebClient webClient, GithubProperties githubProperties,
                               SpreadFactorCalculator spreadFactorCalculator) {
        this.webClient = webClient;
        this.githubProperties = githubProperties;
        this.spreadFactorCalculator = spreadFactorCalculator;
    }

    @Override
    public Mono<LatestRatesResponse> fetchData() {
        return webClient.get()
                .uri("/latest?base=IDR")
                .retrieve()
                .bodyToMono(FrankfurterLatestResponse.class)
                .map(this::transformResponse);
    }

    @Override
    public String getResourceType() {
        return RESOURCE_TYPE;
    }

    private LatestRatesResponse transformResponse(FrankfurterLatestResponse response) {
        double spreadFactor = spreadFactorCalculator.calculateSpreadFactor(githubProperties.getUsername());

        Double usdRate = response.getRates().get("USD");
        Double usdBuySpreadIdr = null;

        if (usdRate != null) {
            usdBuySpreadIdr = spreadFactorCalculator.calculateUSDBuySpreadIdr(usdRate, spreadFactor);
        }

        return new LatestRatesResponse(
                response.getBase(),
                response.getDate(),
                response.getRates(),
                usdBuySpreadIdr
        );
    }
}