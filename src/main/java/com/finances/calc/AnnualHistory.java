package com.finances.calc;

import com.finances.domain.AnnualMetric;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record AnnualHistory(int baseYear, List<Integer> years, Map<AnnualMetric, Map<Integer, BigDecimal>> rows,
                            YearOverview overview) {
}
