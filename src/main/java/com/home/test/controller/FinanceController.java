package com.home.test.controller;

import com.home.test.service.DataStoreService;
import com.home.test.service.CurrencyMetadataService;
import com.home.test.dto.CurrencyResponse;
import com.home.test.dto.EnrichedCurrencyResponse;
import com.home.test.dto.FrankfurterHistoricalResponse;
import com.home.test.dto.LatestRatesResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/finance/data")
public class FinanceController {

    private final DataStoreService dataStoreService;
    private final WebClient webClient;
    private final CurrencyMetadataService currencyMetadataService;

    public FinanceController(DataStoreService dataStoreService, WebClient webClient, CurrencyMetadataService currencyMetadataService) {
        this.dataStoreService = dataStoreService;
        this.webClient = webClient;
        this.currencyMetadataService = currencyMetadataService;
    }

    @GetMapping("/historical/custom")
    public ResponseEntity<Object> getHistoricalData(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam String from,
            @RequestParam String to) {
        if (!dataStoreService.isInitialized()) {
            return ResponseEntity.status(503).body(Map.of(
                    "error", "Service Unavailable",
                    "message", "Data initialization in progress"
            ));
        }

        try {
            // Validate date format (YYYY-MM-DD)
            if (!isValidDate(start) || !isValidDate(end)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Invalid Date Format",
                        "message", "Dates must be in YYYY-MM-DD format"
                ));
            }

            // Validate currency codes
            if (!isValidCurrencyCode(from) || !isValidCurrencyCode(to)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Invalid Currency Code",
                        "message", "Currency codes must be 3-letter ISO 4217 codes"
                ));
            }

            // Fetch historical data from Frankfurter API
            FrankfurterHistoricalResponse response = fetchHistoricalDataFromAPI(start, end, from, to);

            // Sort rates by date in descending order (newest first)
            if (response.getRates() != null && !response.getRates().isEmpty()) {
                Map<String, Map<String, Double>> sortedRates = new java.util.LinkedHashMap<>();
                response.getRates().entrySet().stream()
                    .sorted(Map.Entry.<String, Map<String, Double>>comparingByKey().reversed())
                    .forEachOrdered(entry -> sortedRates.put(entry.getKey(), entry.getValue()));
                response.setRates(sortedRates);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Internal Server Error",
                    "message", "Failed to fetch historical data: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/{resourceType}")
    public ResponseEntity<Object> getData(@PathVariable String resourceType, HttpServletRequest request) {
        if (!dataStoreService.isInitialized()) {
            return ResponseEntity.status(503).body(Map.of(
                    "error", "Service Unavailable",
                    "message", "Data initialization in progress"
            ));
        }

        try {
            switch (resourceType) {
                case "latest_idr_rates":
                    LatestRatesResponse latestRates = dataStoreService.getData(resourceType, LatestRatesResponse.class);
                    return ResponseEntity.ok(latestRates);

                case "supported_currencies":
                    CurrencyResponse currencies = dataStoreService.getData(resourceType, CurrencyResponse.class);
                    // Check if simple format is requested
                    String format = request.getParameter("format");
                    if ("simple".equals(format)) {
                        return ResponseEntity.ok(currencies);
                    }
                    // Default: Enrich response with detailed metadata
                    EnrichedCurrencyResponse enrichedResponse = new EnrichedCurrencyResponse(
                        currencyMetadataService.enrichCurrencies(currencies.getCurrencies())
                    );
                    return ResponseEntity.ok(enrichedResponse);

                default:
                    return ResponseEntity.badRequest().body(Map.of(
                            "error", "Invalid Resource Type",
                            "message", "Valid resource types are: latest_idr_rates, supported_currencies",
                            "provided", resourceType
                    ));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Resource Not Found",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Internal Server Error",
                    "message", "An unexpected error occurred while processing your request"
            ));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", dataStoreService.isInitialized() ? "UP" : "INITIALIZING",
                "initialized", dataStoreService.isInitialized()
        ));
    }

    private boolean isValidDate(String date) {
        try {
            java.time.LocalDate.parse(date);
            return true;
        } catch (java.time.DateTimeException e) {
            return false;
        }
    }

    private boolean isValidCurrencyCode(String currency) {
        return currency != null && currency.matches("^[A-Z]{3}$");
    }

    private FrankfurterHistoricalResponse fetchHistoricalDataFromAPI(String start, String end, String from, String to) {
        String url = String.format("/%s..%s?from=%s&to=%s", start, end, from, to);

        try {
            return webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(FrankfurterHistoricalResponse.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch historical data from Frankfurter API: " + e.getMessage(), e);
        }
    }
}