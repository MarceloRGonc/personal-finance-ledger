package com.finances.calc;

import com.finances.domain.LoanEntry;
import java.math.BigDecimal;
import java.util.List;

public record LoanSummary(String name, BigDecimal principal, BigDecimal opening, BigDecimal paid,
                          BigDecimal remaining, List<LoanEntry> payments) {
}
