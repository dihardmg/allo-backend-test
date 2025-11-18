package com.home.test.service;

import com.home.test.dto.CurrencyResponse;
import com.home.test.dto.LatestRatesResponse;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class DataStoreService {

    private final ConcurrentHashMap<String, Object> dataStore = new ConcurrentHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private volatile boolean initialized = false;

    public void storeLatestRates(LatestRatesResponse data) {
        lock.writeLock().lock();
        try {
            dataStore.put("latest_idr_rates", data);
        } finally {
            lock.writeLock().unlock();
        }
    }

    
    public void storeSupportedCurrencies(CurrencyResponse data) {
        lock.writeLock().lock();
        try {
            dataStore.put("supported_currencies", data);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public <T> T getData(String resourceType, Class<T> clazz) {
        lock.readLock().lock();
        try {
            if (!initialized) {
                throw new IllegalStateException("Data store not initialized yet");
            }
            Object data = dataStore.get(resourceType);
            if (data == null) {
                throw new IllegalArgumentException("Resource type not found: " + resourceType);
            }
            return clazz.cast(data);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void markAsInitialized() {
        lock.writeLock().lock();
        try {
            initialized = true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean isInitialized() {
        lock.readLock().lock();
        try {
            return initialized;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void clearData() {
        lock.writeLock().lock();
        try {
            dataStore.clear();
            initialized = false;
        } finally {
            lock.writeLock().unlock();
        }
    }
}