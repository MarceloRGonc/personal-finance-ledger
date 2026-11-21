package com.finances.calc;

import java.math.BigDecimal;
import java.util.List;

public record ChartData(List<String> labels, List<BigDecimal> values) {
}
