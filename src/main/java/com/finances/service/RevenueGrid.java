package com.finances.service;

import com.finances.domain.RevenueField;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record RevenueGrid(int year, List<RevenueField> fields, Map<Integer, Map<String, BigDecimal>> byMonth,
                          Map<String, BigDecimal> totals) {
}
