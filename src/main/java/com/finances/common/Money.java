package com.finances.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Money {

    private static final BigDecimal LIMIT = new BigDecimal("1000000000000");

    private Money() {
    }

    public static BigDecimal normalize(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal requireReasonable(BigDecimal value) {
        if (value.abs().compareTo(LIMIT) >= 0) {
            throw new IllegalArgumentException("Amount is out of range");
        }
        return normalize(value);
    }

    public static BigDecimal sum(Iterable<BigDecimal> values) {
        var total = BigDecimal.ZERO;
        for (var value : values) {
            total = total.add(value);
        }
        return total;
    }
}
