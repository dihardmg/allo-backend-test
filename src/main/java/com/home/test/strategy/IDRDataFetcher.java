package com.home.test.strategy;

import reactor.core.publisher.Mono;

public interface IDRDataFetcher<T> {
    Mono<T> fetchData();
    String getResourceType();
}