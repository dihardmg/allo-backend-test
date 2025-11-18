package com.home.test.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class FrankfurterLatestResponse {
    private String amount;
    private String base;
    private String date;

    @JsonProperty("rates")
    private Map<String, Double> rates;

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Map<String, Double> getRates() {
        return rates;
    }

    public void setRates(Map<String, Double> rates) {
        this.rates = rates;
    }
}