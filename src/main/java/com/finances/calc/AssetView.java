package com.finances.calc;

import java.math.BigDecimal;
import java.util.Set;

public record AssetView(Long id, String name, BigDecimal baseAmount, BigDecimal contributed, BigDecimal sold,
                        BigDecimal value, Set<String> tags) {
}
