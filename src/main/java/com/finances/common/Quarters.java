package com.finances.common;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

public final class Quarters {

    public static final int COUNT = 4;
    public static final List<Integer> ALL = IntStream.rangeClosed(1, COUNT).boxed().toList();

    private static final int MONTHS_PER_QUARTER = 3;

    private Quarters() {
    }

    public static int ofMonth(int month) {
        return (Months.requireValid(month) - 1) / MONTHS_PER_QUARTER + 1;
    }

    public static int defaultFor(int year, LocalDate today) {
        return year == today.getYear() ? ofMonth(today.getMonthValue()) : 1;
    }

    public static List<Integer> months(int quarter) {
        if (quarter < 1 || quarter > COUNT) {
            throw new IllegalArgumentException("Quarter must be between 1 and " + COUNT);
        }
        var first = (quarter - 1) * MONTHS_PER_QUARTER;
        return Months.ALL.subList(first, first + MONTHS_PER_QUARTER);
    }
}
