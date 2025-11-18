package com.home.test.dto;

import java.util.List;

public class CurrencyResponse {
    private List<String> currencies;

    public CurrencyResponse() {}

    public CurrencyResponse(List<String> currencies) {
        this.currencies = currencies;
    }

    public List<String> getCurrencies() {
        return currencies;
    }

    public void setCurrencies(List<String> currencies) {
        this.currencies = currencies;
    }
}