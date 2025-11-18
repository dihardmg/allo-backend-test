package com.home.test.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CurrencyInfo {
    private String code;
    private String name;
    private String symbol;
    private String country;
    private String countryCode;
    private boolean isBaseCurrency;
    private int decimalPlaces;
    private String displayName;

    public CurrencyInfo(String code, String name, String symbol, String country,
                       String countryCode, boolean isBaseCurrency, int decimalPlaces) {
        this.code = code;
        this.name = name;
        this.symbol = symbol;
        this.country = country;
        this.countryCode = countryCode;
        this.isBaseCurrency = isBaseCurrency;
        this.decimalPlaces = decimalPlaces;
        this.displayName = String.format("%s (%s) - %s", name, code, symbol);
    }

    @JsonProperty("code")
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("symbol")
    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    @JsonProperty("country")
    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @JsonProperty("country_code")
    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    @JsonProperty("is_base_currency")
    public boolean isBaseCurrency() {
        return isBaseCurrency;
    }

    public void setBaseCurrency(boolean baseCurrency) {
        isBaseCurrency = baseCurrency;
    }

    @JsonProperty("decimal_places")
    public int getDecimalPlaces() {
        return decimalPlaces;
    }

    public void setDecimalPlaces(int decimalPlaces) {
        this.decimalPlaces = decimalPlaces;
    }

    @JsonProperty("display_name")
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CurrencyInfo that = (CurrencyInfo) o;
        return code.equals(that.code);
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }

    @Override
    public String toString() {
        return "CurrencyInfo{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", symbol='" + symbol + '\'' +
                ", country='" + country + '\'' +
                '}';
    }
}