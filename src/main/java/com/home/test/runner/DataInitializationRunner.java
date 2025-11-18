package com.home.test.runner;

import com.home.test.service.DataStoreService;
import com.home.test.strategy.LatestRatesStrategy;
import com.home.test.strategy.SupportedCurrenciesStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class DataInitializationRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializationRunner.class);
    private static final Duration INITIALIZATION_TIMEOUT = Duration.ofSeconds(30);

    private final LatestRatesStrategy latestRatesStrategy;
    private final SupportedCurrenciesStrategy supportedCurrenciesStrategy;
    private final DataStoreService dataStoreService;

    public DataInitializationRunner(LatestRatesStrategy latestRatesStrategy,
                                   SupportedCurrenciesStrategy supportedCurrenciesStrategy,
                                   DataStoreService dataStoreService) {
        this.latestRatesStrategy = latestRatesStrategy;
        this.supportedCurrenciesStrategy = supportedCurrenciesStrategy;
        this.dataStoreService = dataStoreService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("Starting data initialization from Frankfurter API...");

        try {
            Mono<Void> initializationMono = Mono.zip(
                    latestRatesStrategy.fetchData()
                            .doOnSuccess(data -> {
                                dataStoreService.storeLatestRates(data);
                                logger.info("Successfully loaded latest IDR rates");
                            })
                            .doOnError(error -> logger.error("Failed to load latest IDR rates", error))
                            .onErrorResume(error -> {
                                logger.warn("Using fallback data for latest rates due to API error: {}", error.getMessage());
                                return Mono.empty(); // Skip this data but continue initialization
                            }),

                    supportedCurrenciesStrategy.fetchData()
                            .doOnSuccess(data -> {
                                dataStoreService.storeSupportedCurrencies(data);
                                logger.info("Successfully loaded supported currencies");
                            })
                            .doOnError(error -> logger.error("Failed to load supported currencies", error))
                            .onErrorResume(error -> {
                                logger.warn("Using fallback data for currencies due to API error: {}", error.getMessage());
                                return Mono.empty(); // Skip this data but continue initialization
                            })
            ).then();

            initializationMono
                    .timeout(INITIALIZATION_TIMEOUT)
                    .doOnSuccess(v -> {
                        // Check if we have at least some data before marking as initialized
                        if (hasAnyData()) {
                            dataStoreService.markAsInitialized();
                            logger.info("Data initialization completed successfully (with fallbacks if needed)");
                        } else {
                            logger.warn("No data could be loaded, but application will start in degraded mode");
                            // Still mark as initialized to allow API to respond with appropriate errors
                            dataStoreService.markAsInitialized();
                        }
                    })
                    .doOnError(error -> {
                        logger.error("Data initialization failed completely", error);
                        dataStoreService.clearData();
                        // Still try to start the application in degraded mode
                        logger.warn("Starting application in degraded mode due to initialization failure");
                        dataStoreService.markAsInitialized();
                    })
                    .block();

        } catch (Exception e) {
            logger.error("Critical error during data initialization", e);
            // Don't throw exception - allow application to start in degraded mode
            logger.warn("Application will start in degraded mode due to initialization errors");
            dataStoreService.markAsInitialized();
        }
    }

    private boolean hasAnyData() {
        try {
            // Simple check if any data was stored
            return dataStoreService.isInitialized();
        } catch (Exception e) {
            return false;
        }
    }
}