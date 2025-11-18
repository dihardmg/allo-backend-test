package com.home.test.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

public class EnrichedCurrencyResponse {
    private List<CurrencyInfo> currencies;
    private CurrencyMetadata metadata;

    public EnrichedCurrencyResponse() {}

    public EnrichedCurrencyResponse(List<CurrencyInfo> currencies) {
        this.currencies = currencies;
        this.metadata = new CurrencyMetadata(currencies.size(), "IDR");
    }

    public List<CurrencyInfo> getCurrencies() {
        return currencies;
    }

    public void setCurrencies(List<CurrencyInfo> currencies) {
        this.currencies = currencies;
    }

    public CurrencyMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(CurrencyMetadata metadata) {
        this.metadata = metadata;
    }

    public static class CurrencyMetadata {
        private int totalCurrencies;
        private int supportedPairs;
        private List<String> baseCurrencies;
        private String lastUpdated;
        private String version;

        public CurrencyMetadata(int totalCurrencies, String baseCurrencyCode) {
            this.totalCurrencies = totalCurrencies;
            this.supportedPairs = totalCurrencies * totalCurrencies; // Simplified calculation
            this.baseCurrencies = List.of(baseCurrencyCode);
            this.lastUpdated = LocalDateTime.now().toString();
            this.version = "2.0.0";
        }

        @JsonProperty("total_currencies")
        public int getTotalCurrencies() {
            return totalCurrencies;
        }

        public void setTotalCurrencies(int totalCurrencies) {
            this.totalCurrencies = totalCurrencies;
        }

        @JsonProperty("supported_pairs")
        public int getSupportedPairs() {
            return supportedPairs;
        }

        public void setSupportedPairs(int supportedPairs) {
            this.supportedPairs = supportedPairs;
        }

        @JsonProperty("base_currencies")
        public List<String> getBaseCurrencies() {
            return baseCurrencies;
        }

        public void setBaseCurrencies(List<String> baseCurrencies) {
            this.baseCurrencies = baseCurrencies;
        }

        @JsonProperty("last_updated")
        public String getLastUpdated() {
            return lastUpdated;
        }

        public void setLastUpdated(String lastUpdated) {
            this.lastUpdated = lastUpdated;
        }

        @JsonProperty("version")
        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }
}