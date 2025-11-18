package com.home.test.util;

import org.springframework.stereotype.Component;

@Component
public class SpreadFactorCalculator {

    public double calculateSpreadFactor(String githubUsername) {
        if (githubUsername == null || githubUsername.isEmpty()) {
            return 0.0;
        }

        String lowerCaseUsername = githubUsername.toLowerCase();
        int sum = 0;

        for (char c : lowerCaseUsername.toCharArray()) {
            sum += (int) c;
        }

        return (sum % 1000) / 100000.0;
    }

    public double calculateUSDBuySpreadIdr(double usdRate, double spreadFactor) {
        if (usdRate == 0) {
            return 0.0;
        }
        return (1.0 / usdRate) * (1.0 + spreadFactor);
    }
}