package com.home.test.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpreadFactorCalculatorTest {

    private SpreadFactorCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new SpreadFactorCalculator();
    }

    @Test
    void testCalculateSpreadFactor_withNormalUsername() {
        String username = "johndoe47";
        double result = calculator.calculateSpreadFactor(username);

        assertTrue(result >= 0.00000 && result <= 0.00999,
            "Spread factor should be between 0.00000 and 0.00999");
    }

    @Test
    void testCalculateSpreadFactor_withEmptyUsername() {
        double result = calculator.calculateSpreadFactor("");
        assertEquals(0.0, result, "Empty username should return 0.0");
    }

    @Test
    void testCalculateSpreadFactor_withNullUsername() {
        double result = calculator.calculateSpreadFactor(null);
        assertEquals(0.0, result, "Null username should return 0.0");
    }

    @Test
    void testCalculateSpreadFactor_withSpecificUsername() {
        String username = "dihardmg";
        double result = calculator.calculateSpreadFactor(username);

        int sum = 0;
        for (char c : username.toLowerCase().toCharArray()) {
            sum += (int) c;
        }

        double expected = (sum % 1000) / 100000.0;
        assertEquals(expected, result, 0.00001, "Spread factor calculation should match expected formula");
    }

    @Test
    void testCalculateUSDBuySpreadIdr_withNormalRate() {
        double usdRate = 0.000065;
        double spreadFactor = 0.001;

        double result = calculator.calculateUSDBuySpreadIdr(usdRate, spreadFactor);

        double expected = (1.0 / usdRate) * (1.0 + spreadFactor);
        assertEquals(expected, result, 0.01, "USD Buy Spread IDR calculation should match expected formula");
    }

    @Test
    void testCalculateUSDBuySpreadIdr_withZeroRate() {
        double usdRate = 0.0;
        double spreadFactor = 0.001;

        double result = calculator.calculateUSDBuySpreadIdr(usdRate, spreadFactor);
        assertEquals(0.0, result, "Zero USD rate should return 0.0");
    }

    @Test
    void testCalculateUSDBuySpreadIdr_withZeroSpreadFactor() {
        double usdRate = 0.000065;
        double spreadFactor = 0.0;

        double result = calculator.calculateUSDBuySpreadIdr(usdRate, spreadFactor);
        double expected = 1.0 / usdRate;
        assertEquals(expected, result, 0.01, "Zero spread factor should return simple reciprocal");
    }
}