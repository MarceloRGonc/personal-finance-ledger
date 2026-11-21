package com.finances.calc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record PortfolioView(List<AssetView> assets, BigDecimal loansRemaining, Map<String, BigDecimal> totals) {
}
