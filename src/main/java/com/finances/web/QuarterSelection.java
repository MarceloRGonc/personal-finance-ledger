package com.finances.web;

import com.finances.common.Quarters;
import java.time.LocalDate;

final class QuarterSelection {

    private QuarterSelection() {
    }

    static int quarter(int year, Integer requested) {
        return requested != null ? requested : Quarters.defaultFor(year, LocalDate.now());
    }

    static Integer currentMonth(int year) {
        var today = LocalDate.now();
        return year == today.getYear() ? today.getMonthValue() : null;
    }
}
