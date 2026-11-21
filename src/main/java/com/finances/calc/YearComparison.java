package com.finances.calc;

import com.finances.domain.AnnualMetric;
import java.math.BigDecimal;
import java.util.Map;

public record YearComparison(int year, Map<AnnualMetric, BigDecimal> values, Map<AnnualMetric, BigDecimal> change,
                             Map<AnnualMetric, BigDecimal> changePercent) {
}
