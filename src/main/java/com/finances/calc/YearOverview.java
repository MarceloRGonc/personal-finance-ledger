package com.finances.calc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record YearOverview(int year, boolean hasData, List<MonthRow> months, Map<String, BigDecimal> totals,
                           Map<String, BigDecimal> yearly) {
}
