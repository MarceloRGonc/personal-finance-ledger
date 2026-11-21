package com.finances.calc;

import java.math.BigDecimal;
import java.util.Map;

public record MonthRow(int month, String label, Map<String, BigDecimal> values) {
}
