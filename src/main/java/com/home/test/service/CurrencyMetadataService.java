package com.home.test.service;

import com.home.test.dto.CurrencyInfo;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CurrencyMetadataService {

    private static final Map<String, CurrencyMetadata> CURRENCY_METADATA = new HashMap<>();

    static {
        // Initialize currency metadata mapping
        CURRENCY_METADATA.put("AUD", new CurrencyMetadata("Australian Dollar", "$", "Australia", "AU", 2));
        CURRENCY_METADATA.put("BGN", new CurrencyMetadata("Bulgarian Lev", "лв", "Bulgaria", "BG", 2));
        CURRENCY_METADATA.put("BRL", new CurrencyMetadata("Brazilian Real", "R$", "Brazil", "BR", 2));
        CURRENCY_METADATA.put("CAD", new CurrencyMetadata("Canadian Dollar", "$", "Canada", "CA", 2));
        CURRENCY_METADATA.put("CHF", new CurrencyMetadata("Swiss Franc", "CHF", "Switzerland", "CH", 2));
        CURRENCY_METADATA.put("CNY", new CurrencyMetadata("Chinese Yuan", "¥", "China", "CN", 2));
        CURRENCY_METADATA.put("CZK", new CurrencyMetadata("Czech Koruna", "Kč", "Czech Republic", "CZ", 2));
        CURRENCY_METADATA.put("DKK", new CurrencyMetadata("Danish Krone", "kr", "Denmark", "DK", 2));
        CURRENCY_METADATA.put("EUR", new CurrencyMetadata("Euro", "€", "European Union", "EU", 2));
        CURRENCY_METADATA.put("GBP", new CurrencyMetadata("British Pound", "£", "United Kingdom", "GB", 2));
        CURRENCY_METADATA.put("HKD", new CurrencyMetadata("Hong Kong Dollar", "$", "Hong Kong", "HK", 2));
        CURRENCY_METADATA.put("HUF", new CurrencyMetadata("Hungarian Forint", "Ft", "Hungary", "HU", 2));
        CURRENCY_METADATA.put("IDR", new CurrencyMetadata("Indonesian Rupiah", "Rp", "Indonesia", "ID", 0));
        CURRENCY_METADATA.put("ILS", new CurrencyMetadata("Israeli New Shekel", "₪", "Israel", "IL", 2));
        CURRENCY_METADATA.put("INR", new CurrencyMetadata("Indian Rupee", "₹", "India", "IN", 2));
        CURRENCY_METADATA.put("ISK", new CurrencyMetadata("Icelandic Króna", "kr", "Iceland", "IS", 0));
        CURRENCY_METADATA.put("JPY", new CurrencyMetadata("Japanese Yen", "¥", "Japan", "JP", 0));
        CURRENCY_METADATA.put("KRW", new CurrencyMetadata("South Korean Won", "₩", "South Korea", "KR", 0));
        CURRENCY_METADATA.put("MXN", new CurrencyMetadata("Mexican Peso", "$", "Mexico", "MX", 2));
        CURRENCY_METADATA.put("MYR", new CurrencyMetadata("Malaysian Ringgit", "RM", "Malaysia", "MY", 2));
        CURRENCY_METADATA.put("NOK", new CurrencyMetadata("Norwegian Krone", "kr", "Norway", "NO", 2));
        CURRENCY_METADATA.put("NZD", new CurrencyMetadata("New Zealand Dollar", "$", "New Zealand", "NZ", 2));
        CURRENCY_METADATA.put("PHP", new CurrencyMetadata("Philippine Peso", "₱", "Philippines", "PH", 2));
        CURRENCY_METADATA.put("PLN", new CurrencyMetadata("Polish Złoty", "zł", "Poland", "PL", 2));
        CURRENCY_METADATA.put("RON", new CurrencyMetadata("Romanian Leu", "lei", "Romania", "RO", 2));
        CURRENCY_METADATA.put("SEK", new CurrencyMetadata("Swedish Krona", "kr", "Sweden", "SE", 2));
        CURRENCY_METADATA.put("SGD", new CurrencyMetadata("Singapore Dollar", "$", "Singapore", "SG", 2));
        CURRENCY_METADATA.put("THB", new CurrencyMetadata("Thai Baht", "฿", "Thailand", "TH", 2));
        CURRENCY_METADATA.put("TRY", new CurrencyMetadata("Turkish Lira", "₺", "Turkey", "TR", 2));
        CURRENCY_METADATA.put("USD", new CurrencyMetadata("United States Dollar", "$", "United States", "US", 2));
        CURRENCY_METADATA.put("ZAR", new CurrencyMetadata("South African Rand", "R", "South Africa", "ZA", 2));
    }

    /**
     * Enrich currency codes with detailed metadata
     */
    public List<CurrencyInfo> enrichCurrencies(List<String> currencyCodes) {
        return currencyCodes.stream()
                .map(this::enrichCurrency)
                .collect(Collectors.toList());
    }

    /**
     * Enrich a single currency code with metadata
     */
    public CurrencyInfo enrichCurrency(String currencyCode) {
        if (currencyCode == null) {
            return null;
        }

        CurrencyMetadata metadata = CURRENCY_METADATA.get(currencyCode.toUpperCase());
        if (metadata == null) {
            // Fallback for unknown currencies
            return new CurrencyInfo(
                currencyCode.toUpperCase(),
                currencyCode.toUpperCase(), // Use code as name
                "", // No symbol
                "Unknown",
                "XX",
                false,
                2
            );
        }

        boolean isBaseCurrency = "IDR".equals(currencyCode.toUpperCase()) || "USD".equals(currencyCode.toUpperCase());

        return new CurrencyInfo(
            currencyCode.toUpperCase(),
            metadata.name,
            metadata.symbol,
            metadata.country,
            metadata.countryCode,
            isBaseCurrency,
            metadata.decimalPlaces
        );
    }

    /**
     * Check if a currency code is supported
     */
    public boolean isSupportedCurrency(String currencyCode) {
        return CURRENCY_METADATA.containsKey(currencyCode.toUpperCase());
    }

    /**
     * Get all supported currency codes
     */
    public List<String> getSupportedCurrencyCodes() {
        return CURRENCY_METADATA.keySet().stream()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Get metadata for a specific currency
     */
    public CurrencyMetadata getCurrencyMetadata(String currencyCode) {
        return CURRENCY_METADATA.get(currencyCode.toUpperCase());
    }

    private static class CurrencyMetadata {
        final String name;
        final String symbol;
        final String country;
        final String countryCode;
        final int decimalPlaces;

        CurrencyMetadata(String name, String symbol, String country, String countryCode, int decimalPlaces) {
            this.name = name;
            this.symbol = symbol;
            this.country = country;
            this.countryCode = countryCode;
            this.decimalPlaces = decimalPlaces;
        }
    }
}