package com.home.test.runner;

import com.home.test.dto.CurrencyResponse;
import com.home.test.dto.LatestRatesResponse;
import com.home.test.service.DataStoreService;
import com.home.test.strategy.LatestRatesStrategy;
import com.home.test.strategy.SupportedCurrenciesStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataInitializationRunnerTest {

    @Mock
    private LatestRatesStrategy latestRatesStrategy;

    @Mock
    private SupportedCurrenciesStrategy supportedCurrenciesStrategy;

    @Mock
    private DataStoreService dataStoreService;

    @Mock
    private ApplicationArguments applicationArguments;

    private DataInitializationRunner runner;

    @BeforeEach
    void setUp() {
        runner = new DataInitializationRunner(
                latestRatesStrategy,
                supportedCurrenciesStrategy,
                dataStoreService
        );
    }

    @Test
    void testRun_SuccessfulInitialization() throws Exception {
        LatestRatesResponse latestRatesResponse = new LatestRatesResponse();
        latestRatesResponse.setBase("IDR");
        latestRatesResponse.setDate("2024-01-01");
        latestRatesResponse.setRates(Map.of("USD", 0.000065));

        CurrencyResponse currencyResponse = new CurrencyResponse(List.of("USD", "EUR", "IDR"));

        when(latestRatesStrategy.fetchData()).thenReturn(Mono.just(latestRatesResponse));
        when(supportedCurrenciesStrategy.fetchData()).thenReturn(Mono.just(currencyResponse));
        when(dataStoreService.isInitialized()).thenReturn(true);

        runner.run(applicationArguments);

        verify(dataStoreService).storeLatestRates(latestRatesResponse);
        verify(dataStoreService).storeSupportedCurrencies(currencyResponse);
        verify(dataStoreService).markAsInitialized();
        verify(dataStoreService, never()).clearData();
    }

    @Test
    void testRun_LatestRatesFailure() throws Exception {
        CurrencyResponse currencyResponse = new CurrencyResponse(List.of("USD", "EUR", "IDR"));

        when(latestRatesStrategy.fetchData()).thenReturn(Mono.error(new RuntimeException("API Error")));
        when(supportedCurrenciesStrategy.fetchData()).thenReturn(Mono.just(currencyResponse));
        when(dataStoreService.isInitialized()).thenReturn(false, true);

        // Should not throw exception anymore - just handle gracefully
        assertDoesNotThrow(() -> runner.run(applicationArguments));

        // Should NOT clear data on individual strategy failure (handled gracefully with onErrorResume)
        verify(dataStoreService, never()).clearData();
        // Should still mark as initialized to allow degraded mode
        verify(dataStoreService).markAsInitialized();
    }

    @Test
    void testRun_SupportedCurrenciesFailure() throws Exception {
        LatestRatesResponse latestRatesResponse = new LatestRatesResponse();
        latestRatesResponse.setBase("IDR");
        latestRatesResponse.setDate("2024-01-01");
        latestRatesResponse.setRates(Map.of("USD", 0.000065));

        when(latestRatesStrategy.fetchData()).thenReturn(Mono.just(latestRatesResponse));
        when(supportedCurrenciesStrategy.fetchData()).thenReturn(Mono.error(new RuntimeException("API Error")));
        when(dataStoreService.isInitialized()).thenReturn(false, true);

        // Should not throw exception anymore - just handle gracefully
        assertDoesNotThrow(() -> runner.run(applicationArguments));

        // Should NOT clear data on individual strategy failure (handled gracefully with onErrorResume)
        verify(dataStoreService, never()).clearData();
        // But should still mark as initialized to allow degraded mode
        verify(dataStoreService).markAsInitialized();
    }

    @Test
    void testRun_Timeout() throws Exception {
        when(latestRatesStrategy.fetchData()).thenReturn(Mono.never());
        when(supportedCurrenciesStrategy.fetchData()).thenReturn(Mono.never());

        // Should not throw exception anymore - just handle gracefully
        assertDoesNotThrow(() -> runner.run(applicationArguments));

        // Should clear data on timeout (this triggers the doOnError block)
        verify(dataStoreService, times(1)).clearData();
        // Should mark as initialized twice: once in doOnError, once in catch block
        verify(dataStoreService, times(2)).markAsInitialized();
    }

    @Test
    void testRun_EmptyResponses() throws Exception {
        LatestRatesResponse latestRatesResponse = new LatestRatesResponse();
        CurrencyResponse currencyResponse = new CurrencyResponse(List.of());

        when(latestRatesStrategy.fetchData()).thenReturn(Mono.just(latestRatesResponse));
        when(supportedCurrenciesStrategy.fetchData()).thenReturn(Mono.just(currencyResponse));
        when(dataStoreService.isInitialized()).thenReturn(false, true);

        runner.run(applicationArguments);

        verify(dataStoreService).storeLatestRates(latestRatesResponse);
        verify(dataStoreService).storeSupportedCurrencies(currencyResponse);
        verify(dataStoreService).markAsInitialized();
        verify(dataStoreService, never()).clearData();
    }
}
