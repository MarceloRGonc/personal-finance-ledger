package com.finances.common;

import java.time.Year;

public final class Periods {

    /** The year whose figures are typed in by hand. Every later year is calculated from it. */
    public static final int BASE_YEAR = 2023;

    private static final int MIN_YEAR = 1970;
    private static final int MAX_YEAR = 2200;

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
