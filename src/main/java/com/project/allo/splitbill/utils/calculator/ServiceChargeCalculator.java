package com.project.allo.splitbill.utils.calculator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

import org.springframework.stereotype.Component;

@Component
public class ServiceChargeCalculator {

    public int calcServiceChargePct() {
        String username = "umiseldaa";
        int sum = 0;
        char[] chr = username.toLowerCase().toCharArray();

        for (int i = 0; i < chr.length; i++) {
            sum += (int) chr[i];
        }

        return sum % 10;
    }

    public BigDecimal calcServiceChargeAmount(BigDecimal totalExpense, int serviceChargePct) {
        BigDecimal pct = BigDecimal.valueOf(serviceChargePct);
        return totalExpense.multiply(pct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

}
