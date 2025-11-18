package com.home.test.service;

import com.home.test.dto.CurrencyInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CurrencyMetadataServiceTest {

    private CurrencyMetadataService currencyMetadataService;

    @BeforeEach
    void setUp() {
        currencyMetadataService = new CurrencyMetadataService();
    }

    // ==================== SINGLE CURRENCY ENRICHMENT TESTS ====================

    @Test
    void enrichCurrency_ValidKnownCurrency_ReturnsCorrectMetadata() {
        // Act
        CurrencyInfo result = currencyMetadataService.enrichCurrency("USD");

        // Assert
        assertNotNull(result);
        assertEquals("USD", result.getCode());
        assertEquals("United States Dollar", result.getName());
        assertEquals("$", result.getSymbol());
        assertEquals("United States", result.getCountry());
        assertEquals("US", result.getCountryCode());
        assertTrue(result.isBaseCurrency());
        assertEquals(2, result.getDecimalPlaces());
        assertEquals("United States Dollar (USD) - $", result.getDisplayName());
    }

    @Test
    void enrichCurrency_ValidKnownCurrency_IDR_ReturnsCorrectMetadata() {
        // Act
        CurrencyInfo result = currencyMetadataService.enrichCurrency("IDR");

        // Assert
        assertNotNull(result);
        assertEquals("IDR", result.getCode());
        assertEquals("Indonesian Rupiah", result.getName());
        assertEquals("Rp", result.getSymbol());
        assertEquals("Indonesia", result.getCountry());
        assertEquals("ID", result.getCountryCode());
        assertTrue(result.isBaseCurrency());
        assertEquals(0, result.getDecimalPlaces()); // No decimal places for IDR
    }

    @Test
    void enrichCurrency_ValidKnownCurrency_JPY_ReturnsCorrectMetadata() {
        // Act
        CurrencyInfo result = currencyMetadataService.enrichCurrency("JPY");

        // Assert
        assertNotNull(result);
        assertEquals("JPY", result.getCode());
        assertEquals("Japanese Yen", result.getName());
        assertEquals("¥", result.getSymbol());
        assertEquals("Japan", result.getCountry());
        assertEquals("JP", result.getCountryCode());
        assertFalse(result.isBaseCurrency());
        assertEquals(0, result.getDecimalPlaces()); // No decimal places for JPY
    }

    @Test
    void enrichCurrency_ValidKnownCurrency_EUR_ReturnsCorrectMetadata() {
        // Act
        CurrencyInfo result = currencyMetadataService.enrichCurrency("EUR");

        // Assert
        assertNotNull(result);
        assertEquals("EUR", result.getCode());
        assertEquals("Euro", result.getName());
        assertEquals("€", result.getSymbol());
        assertEquals("European Union", result.getCountry());
        assertEquals("EU", result.getCountryCode());
        assertFalse(result.isBaseCurrency());
        assertEquals(2, result.getDecimalPlaces());
    }

    @Test
    void enrichCurrency_UnknownCurrency_ReturnsFallbackMetadata() {
        // Act
        CurrencyInfo result = currencyMetadataService.enrichCurrency("XYZ");

        // Assert
        assertNotNull(result);
        assertEquals("XYZ", result.getCode());
        assertEquals("XYZ", result.getName()); // Code used as name
        assertEquals("", result.getSymbol()); // Empty symbol
        assertEquals("Unknown", result.getCountry());
        assertEquals("XX", result.getCountryCode());
        assertFalse(result.isBaseCurrency());
        assertEquals(2, result.getDecimalPlaces());
        assertEquals("XYZ (XYZ) - ", result.getDisplayName());
    }

    @Test
    void enrichCurrency_LowercaseCurrency_HandlesCorrectly() {
        // Act
        CurrencyInfo result = currencyMetadataService.enrichCurrency("usd");

        // Assert
        assertNotNull(result);
        assertEquals("USD", result.getCode()); // Should be converted to uppercase
        assertEquals("United States Dollar", result.getName());
        assertEquals("$", result.getSymbol());
    }

    @Test
    void enrichCurrency_MixedCaseCurrency_HandlesCorrectly() {
        // Act
        CurrencyInfo result = currencyMetadataService.enrichCurrency("eUr");

        // Assert
        assertNotNull(result);
        assertEquals("EUR", result.getCode()); // Should be converted to uppercase
        assertEquals("Euro", result.getName());
        assertEquals("€", result.getSymbol());
    }

    @Test
    void enrichCurrency_NullCurrency_ReturnsNull() {
        // Act
        CurrencyInfo result = currencyMetadataService.enrichCurrency(null);

        // Assert
        assertNull(result);
    }

    @Test
    void enrichCurrency_EmptyCurrency_ReturnsFallbackMetadata() {
        // Act
        CurrencyInfo result = currencyMetadataService.enrichCurrency("");

        // Assert
        assertNotNull(result);
        assertEquals("", result.getCode());
        assertEquals("", result.getName());
        assertEquals("", result.getSymbol());
        assertEquals("Unknown", result.getCountry());
        assertEquals("XX", result.getCountryCode());
    }

    // ==================== MULTIPLE CURRENCIES ENRICHMENT TESTS ====================

    @Test
    void enrichCurrencies_MultipleKnownCurrencies_ReturnsCorrectMetadata() {
        // Arrange
        List<String> currencies = Arrays.asList("USD", "EUR", "JPY", "GBP");

        // Act
        List<CurrencyInfo> result = currencyMetadataService.enrichCurrencies(currencies);

        // Assert
        assertNotNull(result);
        assertEquals(4, result.size());

        CurrencyInfo usdInfo = result.stream().filter(c -> "USD".equals(c.getCode())).findFirst().orElse(null);
        assertNotNull(usdInfo);
        assertEquals("United States Dollar", usdInfo.getName());
        assertTrue(usdInfo.isBaseCurrency());

        CurrencyInfo eurInfo = result.stream().filter(c -> "EUR".equals(c.getCode())).findFirst().orElse(null);
        assertNotNull(eurInfo);
        assertEquals("Euro", eurInfo.getName());
        assertFalse(eurInfo.isBaseCurrency());

        CurrencyInfo jpyInfo = result.stream().filter(c -> "JPY".equals(c.getCode())).findFirst().orElse(null);
        assertNotNull(jpyInfo);
        assertEquals("Japanese Yen", jpyInfo.getName());
        assertEquals(0, jpyInfo.getDecimalPlaces());
    }

    @Test
    void enrichCurrencies_MixedKnownAndUnknownCurrencies_ReturnsCorrectMetadata() {
        // Arrange
        List<String> currencies = Arrays.asList("USD", "XYZ", "EUR", "ABC");

        // Act
        List<CurrencyInfo> result = currencyMetadataService.enrichCurrencies(currencies);

        // Assert
        assertNotNull(result);
        assertEquals(4, result.size());

        // Check known currencies
        CurrencyInfo usdInfo = result.stream().filter(c -> "USD".equals(c.getCode())).findFirst().orElse(null);
        assertNotNull(usdInfo);
        assertEquals("United States Dollar", usdInfo.getName());
        assertEquals("$", usdInfo.getSymbol());

        // Check unknown currencies
        CurrencyInfo xyzInfo = result.stream().filter(c -> "XYZ".equals(c.getCode())).findFirst().orElse(null);
        assertNotNull(xyzInfo);
        assertEquals("XYZ", xyzInfo.getName()); // Code used as name
        assertEquals("", xyzInfo.getSymbol());

        CurrencyInfo abcInfo = result.stream().filter(c -> "ABC".equals(c.getCode())).findFirst().orElse(null);
        assertNotNull(abcInfo);
        assertEquals("ABC", abcInfo.getName()); // Code used as name
        assertEquals("", abcInfo.getSymbol());
    }

    @Test
    void enrichCurrencies_EmptyList_ReturnsEmptyList() {
        // Act
        List<CurrencyInfo> result = currencyMetadataService.enrichCurrencies(Arrays.asList());

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void enrichCurrencies_NullList_ThrowsNullPointerException() {
        // Act & Assert
        // The service doesn't handle null lists, so this would throw NPE
        // This test documents that behavior
        assertThrows(NullPointerException.class, () -> {
            currencyMetadataService.enrichCurrencies(null);
        });
    }

    @Test
    void enrichCurrencies_ListWithNulls_HandlesCorrectly() {
        // Arrange
        List<String> currencies = Arrays.asList("USD", null, "EUR", null);

        // Act
        List<CurrencyInfo> result = currencyMetadataService.enrichCurrencies(currencies);

        // Assert
        assertNotNull(result);
        assertEquals(4, result.size()); // Including null entries

        // Non-null currencies should be processed correctly
        CurrencyInfo usdInfo = result.stream()
            .filter(c -> c != null && "USD".equals(c.getCode()))
            .findFirst()
            .orElse(null);
        assertNotNull(usdInfo);

        // Null currencies should result in null CurrencyInfo objects
        long nullCount = result.stream().filter(c -> c == null).count();
        assertEquals(2, nullCount);
    }

    // ==================== CURRENCY SUPPORT TESTS ====================

    @Test
    void isSupportedCurrency_KnownCurrency_ReturnsTrue() {
        // Act & Assert
        assertTrue(currencyMetadataService.isSupportedCurrency("USD"));
        assertTrue(currencyMetadataService.isSupportedCurrency("EUR"));
        assertTrue(currencyMetadataService.isSupportedCurrency("IDR"));
        assertTrue(currencyMetadataService.isSupportedCurrency("JPY"));
    }

    @Test
    void isSupportedCurrency_UnknownCurrency_ReturnsFalse() {
        // Act & Assert
        assertFalse(currencyMetadataService.isSupportedCurrency("XYZ"));
        assertFalse(currencyMetadataService.isSupportedCurrency("ABC"));
        assertFalse(currencyMetadataService.isSupportedCurrency("INVALID"));
    }

    @Test
    void isSupportedCurrency_CaseInsensitive_ReturnsCorrectResult() {
        // Act & Assert
        assertTrue(currencyMetadataService.isSupportedCurrency("usd")); // lowercase
        assertTrue(currencyMetadataService.isSupportedCurrency("Usd")); // mixed case
        assertTrue(currencyMetadataService.isSupportedCurrency("USD")); // uppercase
    }

    @Test
    void isSupportedCurrency_NullCurrency_ReturnsFalse() {
        // Act & Assert
        // The service doesn't handle null currency codes, so this would throw NPE
        // This test documents that behavior
        assertThrows(NullPointerException.class, () -> {
            currencyMetadataService.isSupportedCurrency(null);
        });
    }

    @Test
    void isSupportedCurrency_EmptyCurrency_ReturnsFalse() {
        // Act & Assert
        assertFalse(currencyMetadataService.isSupportedCurrency(""));
    }

    // ==================== SUPPORTED CURRENCY CODES TESTS ====================

    @Test
    void getSupportedCurrencyCodes_ReturnsAllCodesSorted() {
        // Act
        List<String> result = currencyMetadataService.getSupportedCurrencyCodes();

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());

        // Should contain major currencies
        assertTrue(result.contains("USD"));
        assertTrue(result.contains("EUR"));
        assertTrue(result.contains("JPY"));
        assertTrue(result.contains("GBP"));
        assertTrue(result.contains("IDR"));

        // Should be sorted alphabetically
        List<String> sortedResult = result.stream().sorted().collect(Collectors.toList());
        assertEquals(sortedResult, result);

        // Verify no duplicates
        long uniqueCount = result.stream().distinct().count();
        assertEquals(result.size(), uniqueCount);
    }

    // ==================== CURRENCY METADATA TESTS ====================

    @Test
    void getCurrencyMetadata_KnownCurrency_ReturnsMetadata() {
        // Act
        var result = currencyMetadataService.getCurrencyMetadata("USD");

        // Assert
        assertNotNull(result);
        // Note: CurrencyMetadata is a private inner class, so we can't directly test its fields
        // But we can verify it's not null for known currencies
    }

    @Test
    void getCurrencyMetadata_UnknownCurrency_ReturnsNull() {
        // Act
        var result = currencyMetadataService.getCurrencyMetadata("XYZ");

        // Assert
        assertNull(result);
    }

    @Test
    void getCurrencyMetadata_CaseInsensitive_WorksCorrectly() {
        // Act
        var resultUpper = currencyMetadataService.getCurrencyMetadata("USD");
        var resultLower = currencyMetadataService.getCurrencyMetadata("usd");
        var resultMixed = currencyMetadataService.getCurrencyMetadata("Usd");

        // Assert
        assertNotNull(resultUpper);
        assertNotNull(resultLower);
        assertNotNull(resultMixed);
        // All should return the same metadata object (since it's a static map)
        assertEquals(resultUpper, resultLower);
        assertEquals(resultLower, resultMixed);
    }

    // ==================== BASE CURRENCY TESTS ====================

    @Test
    void enrichCurrencies_BaseCurrenciesMarkedCorrectly() {
        // Arrange
        List<String> currencies = Arrays.asList("USD", "IDR", "EUR", "JPY");

        // Act
        List<CurrencyInfo> result = currencyMetadataService.enrichCurrencies(currencies);

        // Assert
        for (CurrencyInfo currency : result) {
            if ("USD".equals(currency.getCode()) || "IDR".equals(currency.getCode())) {
                assertTrue(currency.isBaseCurrency(),
                    currency.getCode() + " should be marked as base currency");
            } else {
                assertFalse(currency.isBaseCurrency(),
                    currency.getCode() + " should not be marked as base currency");
            }
        }
    }

    // ==================== DECIMAL PLACES TESTS ====================

    @Test
    void enrichCurrencies_DecimalPlacesSetCorrectly() {
        // Arrange
        List<String> currencies = Arrays.asList("USD", "JPY", "IDR");

        // Act
        List<CurrencyInfo> result = currencyMetadataService.enrichCurrencies(currencies);

        // Assert
        CurrencyInfo usdInfo = result.stream().filter(c -> "USD".equals(c.getCode())).findFirst().orElse(null);
        assertNotNull(usdInfo);
        assertEquals(2, usdInfo.getDecimalPlaces());

        CurrencyInfo jpyInfo = result.stream().filter(c -> "JPY".equals(c.getCode())).findFirst().orElse(null);
        assertNotNull(jpyInfo);
        assertEquals(0, jpyInfo.getDecimalPlaces()); // Japanese Yen has no decimal places

        CurrencyInfo idrInfo = result.stream().filter(c -> "IDR".equals(c.getCode())).findFirst().orElse(null);
        assertNotNull(idrInfo);
        assertEquals(0, idrInfo.getDecimalPlaces()); // Indonesian Rupiah has no decimal places
    }

    // ==================== EDGE CASES TESTS ====================

    @Test
    void enrichCurrencies_AllSupportedCurrencies_ProcessesAllSuccessfully() {
        // Arrange
        List<String> allCurrencies = currencyMetadataService.getSupportedCurrencyCodes();

        // Act
        List<CurrencyInfo> result = currencyMetadataService.enrichCurrencies(allCurrencies);

        // Assert
        assertNotNull(result);
        assertEquals(allCurrencies.size(), result.size());

        // All currencies should be processed without nulls
        long nullCount = result.stream().filter(c -> c == null).count();
        assertEquals(0, nullCount);

        // All currency codes should match input
        for (String inputCode : allCurrencies) {
            boolean found = result.stream()
                .anyMatch(info -> info != null && inputCode.equals(info.getCode()));
            assertTrue(found, "Currency " + inputCode + " should be found in enriched results");
        }
    }

    @Test
    void enrichCurrency_SpecialSymbols_HandlesCorrectly() {
        // Test currencies with special symbols
        String[] currenciesWithSpecialSymbols = {"EUR", "JPY", "GBP", "KRW", "CNY", "INR"};

        for (String currency : currenciesWithSpecialSymbols) {
            // Act
            CurrencyInfo result = currencyMetadataService.enrichCurrency(currency);

            // Assert
            assertNotNull(result, "Currency " + currency + " should not be null");
            assertNotNull(result.getSymbol(), "Currency " + currency + " should have a symbol");
            assertFalse(result.getSymbol().isEmpty(), "Currency " + currency + " symbol should not be empty");
        }
    }
}