package com.home.test.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Map;

public class FrankfurterHistoricalResponse {
    @JsonProperty("amount")
    private String amount;

    @JsonProperty("base")
    private String base;

    @JsonProperty("start_date")
    private String startDate;

    @JsonProperty("end_date")
    private String endDate;

    @JsonProperty("rates")
    private Map<String, Map<String, Double>> rates;

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

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public Map<String, Map<String, Double>> getRates() {
        return rates;
    }

    public void setRates(Map<String, Map<String, Double>> rates) {
        this.rates = rates;
    }
}