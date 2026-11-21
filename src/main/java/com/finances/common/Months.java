package com.finances.common;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

public final class Months {

    public static final List<Integer> ALL = IntStream.rangeClosed(1, 12).boxed().toList();

    private Months() {
    }

    public static String label(int month) {
        return Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    }

    public static int requireValid(int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }
        return month;
    }
}
