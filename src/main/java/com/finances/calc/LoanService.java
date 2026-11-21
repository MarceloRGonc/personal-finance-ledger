package com.finances.calc;

import com.finances.domain.LoanEntry;
import com.finances.domain.LoanEntryKind;
import com.finances.repository.LoanEntryRepository;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LoanService {

    private final LoanEntryRepository entries;

    public LoanService(LoanEntryRepository entries) {
        this.entries = entries;
    }

    public List<LoanSummary> summaries() {
        Map<String, List<LoanEntry>> byLoan = entries.findAllByOrderByLoanNameAscSortOrderAscIdAsc().stream()
                .collect(Collectors.groupingBy(LoanEntry::getLoanName, LinkedHashMap::new, Collectors.toList()));
        return byLoan.entrySet().stream().map(e -> summarize(e.getKey(), e.getValue())).toList();
    }

    public BigDecimal remainingTotal() {
        return summaries().stream().map(LoanSummary::remaining).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static LoanSummary summarize(String name, List<LoanEntry> all) {
        var principal = sum(all, LoanEntryKind.PRINCIPAL);
        var opening = all.stream().filter(e -> e.getKind() == LoanEntryKind.OPENING)
                .map(LoanEntry::getAmount).findFirst().orElse(null);
        var paid = all.stream().filter(e -> e.getKind() == LoanEntryKind.PAYMENT && e.isCounted())
                .map(LoanEntry::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        var start = opening != null ? opening : principal;
        var payments = all.stream().filter(e -> e.getKind() == LoanEntryKind.PAYMENT).toList();
        return new LoanSummary(name, principal, opening, paid, start.subtract(paid), payments);
    }

    private static BigDecimal sum(List<LoanEntry> all, LoanEntryKind kind) {
        return all.stream().filter(e -> e.getKind() == kind).map(LoanEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
