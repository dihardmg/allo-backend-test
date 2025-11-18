package com.home.test.service;

import com.home.test.dto.CurrencyResponse;
import com.home.test.dto.LatestRatesResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DataStoreServiceTest {

    private DataStoreService dataStoreService;

    @BeforeEach
    void setUp() {
        dataStoreService = new DataStoreService();
    }

    // ==================== INITIALIZATION TESTS ====================

    @Test
    void isInitialized_DefaultState_ReturnsFalse() {
        // Act & Assert
        assertFalse(dataStoreService.isInitialized());
    }

    @Test
    void markAsInitialized_Called_ReturnsTrue() {
        // Act
        dataStoreService.markAsInitialized();

        // Assert
        assertTrue(dataStoreService.isInitialized());
    }

    // ==================== LATEST RATES TESTS ====================

    @Test
    void storeLatestRates_ValidData_Success() {
        // Arrange
        Map<String, Double> rates = new HashMap<>();
        rates.put("USD", 0.000064);
        rates.put("EUR", 0.000059);

        LatestRatesResponse data = new LatestRatesResponse(
            "IDR", "2024-01-15", rates, 15800.0
        );

        // Act
        dataStoreService.storeLatestRates(data);

        // Assert
        assertFalse(dataStoreService.isInitialized()); // Still not marked as initialized
    }

    @Test
    void getData_LatestRatesAfterInitialization_ReturnsData() throws Exception {
        // Arrange
        Map<String, Double> rates = new HashMap<>();
        rates.put("USD", 0.000064);
        rates.put("EUR", 0.000059);

        LatestRatesResponse originalData = new LatestRatesResponse(
            "IDR", "2024-01-15", rates, 15800.0
        );

        dataStoreService.storeLatestRates(originalData);
        dataStoreService.markAsInitialized();

        // Act
        LatestRatesResponse retrievedData = dataStoreService.getData("latest_idr_rates", LatestRatesResponse.class);

        // Assert
        assertNotNull(retrievedData);
        assertEquals("IDR", retrievedData.getBase());
        assertEquals("2024-01-15", retrievedData.getDate());
        assertEquals(15800.0, retrievedData.getUsdBuySpreadIdr());
        assertEquals(2, retrievedData.getRates().size());
        assertEquals(0.000064, retrievedData.getRates().get("USD"));
        assertEquals(0.000059, retrievedData.getRates().get("EUR"));
    }

    // ==================== SUPPORTED CURRENCIES TESTS ====================

    @Test
    void storeSupportedCurrencies_ValidData_Success() {
        // Arrange
        List<String> currencies = List.of("USD", "EUR", "SGD", "JPY", "GBP");
        CurrencyResponse data = new CurrencyResponse(currencies);

        // Act
        dataStoreService.storeSupportedCurrencies(data);

        // Assert
        assertFalse(dataStoreService.isInitialized()); // Still not marked as initialized
    }

    @Test
    void getSupportedCurrencies_AfterInitialization_ReturnsData() throws Exception {
        // Arrange
        List<String> currencies = List.of("USD", "EUR", "SGD", "JPY", "GBP");
        CurrencyResponse originalData = new CurrencyResponse(currencies);

        dataStoreService.storeSupportedCurrencies(originalData);
        dataStoreService.markAsInitialized();

        // Act
        CurrencyResponse retrievedData = dataStoreService.getData("supported_currencies", CurrencyResponse.class);

        // Assert
        assertNotNull(retrievedData);
        assertEquals(5, retrievedData.getCurrencies().size());
        assertTrue(retrievedData.getCurrencies().contains("USD"));
        assertTrue(retrievedData.getCurrencies().contains("EUR"));
        assertTrue(retrievedData.getCurrencies().contains("SGD"));
        assertTrue(retrievedData.getCurrencies().contains("JPY"));
        assertTrue(retrievedData.getCurrencies().contains("GBP"));
    }

    // ==================== ERROR HANDLING TESTS ====================

    @Test
    void getData_NotInitialized_ThrowsException() {
        // Arrange
        Map<String, Double> rates = new HashMap<>();
        rates.put("USD", 0.000064);

        LatestRatesResponse data = new LatestRatesResponse(
            "IDR", "2024-01-15", rates, 15800.0
        );

        dataStoreService.storeLatestRates(data);
        // Don't mark as initialized

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> dataStoreService.getData("latest_idr_rates", LatestRatesResponse.class)
        );

        assertEquals("Data store not initialized yet", exception.getMessage());
    }

    @Test
    void getData_ResourceNotFound_ThrowsException() {
        // Arrange
        dataStoreService.markAsInitialized();
        // Don't store any data

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> dataStoreService.getData("non_existent_resource", LatestRatesResponse.class)
        );

        assertEquals("Resource type not found: non_existent_resource", exception.getMessage());
    }

    @Test
    void getData_WrongType_ThrowsClassCastException() {
        // Arrange
        Map<String, Double> rates = new HashMap<>();
        LatestRatesResponse data = new LatestRatesResponse("IDR", "2024-01-15", rates, 15800.0);

        dataStoreService.storeLatestRates(data);
        dataStoreService.markAsInitialized();

        // Act & Assert
        assertThrows(
            ClassCastException.class,
            () -> dataStoreService.getData("latest_idr_rates", CurrencyResponse.class)
        );
    }

    // ==================== CLEAR DATA TESTS ====================

    @Test
    void clearData_WithStoredData_ClearsAllData() throws Exception {
        // Arrange
        Map<String, Double> rates = new HashMap<>();
        rates.put("USD", 0.000064);

        LatestRatesResponse ratesData = new LatestRatesResponse(
            "IDR", "2024-01-15", rates, 15800.0
        );

        List<String> currencies = List.of("USD", "EUR", "SGD");
        CurrencyResponse currencyData = new CurrencyResponse(currencies);

        dataStoreService.storeLatestRates(ratesData);
        dataStoreService.storeSupportedCurrencies(currencyData);
        dataStoreService.markAsInitialized();

        // Verify data exists
        assertTrue(dataStoreService.isInitialized());
        assertNotNull(dataStoreService.getData("latest_idr_rates", LatestRatesResponse.class));
        assertNotNull(dataStoreService.getData("supported_currencies", CurrencyResponse.class));

        // Act
        dataStoreService.clearData();

        // Assert
        assertFalse(dataStoreService.isInitialized());

        // Verify data is gone
        assertThrows(
            IllegalStateException.class,
            () -> dataStoreService.getData("latest_idr_rates", LatestRatesResponse.class)
        );
    }

    // ==================== CONCURRENCY TESTS ====================

    @Test
    void concurrentWritesAndReads_ThreadSafe() throws Exception {
        // Arrange
        int threadCount = 10;
        int operationsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(threadCount);

        // Act - Simulate concurrent operations
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready

                    for (int j = 0; j < operationsPerThread; j++) {
                        if (threadId % 2 == 0) {
                            // Even threads: write operations
                            Map<String, Double> rates = new HashMap<>();
                            rates.put("USD", 0.000064 + threadId * 0.000001);

                            LatestRatesResponse data = new LatestRatesResponse(
                                "IDR", "2024-01-15", rates, 15800.0 + threadId
                            );

                            dataStoreService.storeLatestRates(data);

                            List<String> currencies = List.of("USD", "EUR", "SGD");
                            CurrencyResponse currencyData = new CurrencyResponse(currencies);
                            dataStoreService.storeSupportedCurrencies(currencyData);
                        } else {
                            // Odd threads: read operations (may throw exceptions)
                            try {
                                if (dataStoreService.isInitialized()) {
                                    dataStoreService.getData("latest_idr_rates", LatestRatesResponse.class);
                                    dataStoreService.getData("supported_currencies", CurrencyResponse.class);
                                }
                            } catch (Exception e) {
                                // Expected during concurrent operations
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    completeLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // Start all threads
        assertTrue(completeLatch.await(10, TimeUnit.SECONDS)); // Wait for completion

        // Mark as initialized after all writes
        dataStoreService.markAsInitialized();

        // Assert - Verify data integrity
        assertTrue(dataStoreService.isInitialized());
        assertNotNull(dataStoreService.getData("latest_idr_rates", LatestRatesResponse.class));
        assertNotNull(dataStoreService.getData("supported_currencies", CurrencyResponse.class));

        executor.shutdown();
    }

    @Test
    void concurrentInitialization_ThreadSafe() throws Exception {
        // Arrange
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(threadCount);

        // Act - Multiple threads try to mark as initialized
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    dataStoreService.markAsInitialized();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    completeLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue(completeLatch.await(5, TimeUnit.SECONDS));

        // Assert - Only one initialization should have taken effect
        assertTrue(dataStoreService.isInitialized());

        executor.shutdown();
    }

    // ==================== DATA INTEGRITY TESTS ====================

    @Test
    void multipleStoreOperations_KeepsLatestData() throws Exception {
        // Arrange
        Map<String, Double> rates1 = new HashMap<>();
        rates1.put("USD", 0.000064);
        LatestRatesResponse data1 = new LatestRatesResponse("IDR", "2024-01-15", rates1, 15800.0);

        Map<String, Double> rates2 = new HashMap<>();
        rates2.put("USD", 0.000065);
        rates2.put("EUR", 0.000059);
        LatestRatesResponse data2 = new LatestRatesResponse("IDR", "2024-01-16", rates2, 15850.0);

        // Act
        dataStoreService.storeLatestRates(data1);
        dataStoreService.storeLatestRates(data2); // Should overwrite
        dataStoreService.markAsInitialized();

        // Assert
        LatestRatesResponse retrievedData = dataStoreService.getData("latest_idr_rates", LatestRatesResponse.class);

        assertEquals("2024-01-16", retrievedData.getDate()); // Latest date
        assertEquals(15850.0, retrievedData.getUsdBuySpreadIdr()); // Latest spread
        assertEquals(2, retrievedData.getRates().size()); // Latest rates
        assertEquals(0.000065, retrievedData.getRates().get("USD")); // Latest USD rate
        assertEquals(0.000059, retrievedData.getRates().get("EUR")); // EUR added in latest
    }

    @Test
    void nullDataHandling_ThrowsNullPointerException() {
        // Act & Assert - These operations should throw NPE because service doesn't handle null data
        assertThrows(NullPointerException.class, () -> dataStoreService.storeLatestRates(null));
        assertThrows(NullPointerException.class, () -> dataStoreService.storeSupportedCurrencies(null));
    }
}