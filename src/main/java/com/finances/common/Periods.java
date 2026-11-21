package com.finances.common;

import java.time.Year;

public final class Periods {

    public static final int MIN_YEAR = 1970;
    public static final int MAX_YEAR = 2200;

    private Periods() {
    }

    public static int requireYear(int year) {
        if (year < MIN_YEAR || year > MAX_YEAR) {
            throw new IllegalArgumentException("Year must be between " + MIN_YEAR + " and " + MAX_YEAR);
        }
        return year;
    }

    public static int yearOrCurrent(Integer year) {
        return requireYear(year == null ? Year.now().getValue() : year);
    }
}
