package com.finances.service;

import com.finances.common.Money;
import com.finances.common.Months;
import com.finances.common.Periods;
import com.finances.domain.RevenueLine;
import com.finances.repository.RevenueLineRepository;
import java.math.BigDecimal;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RevenueLineService {

    public static final int QUARTERS = 4;
    private static final int MONTHS_PER_QUARTER = 3;

    private final RevenueLineRepository lines;

    public RevenueLineService(RevenueLineRepository lines) {
        this.lines = lines;
    }

    @Transactional(readOnly = true)
    public RevenueLineGrid grid(int year, int quarter) {
        if (quarter < 1 || quarter > QUARTERS) {
            throw new IllegalArgumentException("Quarter must be between 1 and " + QUARTERS);
        }
        var first = (quarter - 1) * MONTHS_PER_QUARTER;
        var columns = Months.ALL.subList(first, first + MONTHS_PER_QUARTER).stream().map(month -> {
            var items = lines.findByYearAndMonthOrderBySortOrderAscIdAsc(year, month);
            var total = Money.sum(items.stream().map(RevenueLine::getAmount).toList());
            return new RevenueLineGrid.MonthColumn(month, Months.label(month), items, total);
        }).toList();
        return new RevenueLineGrid(year, quarter, columns);
    }

    public RevenueLine add(int year, int month, String description, BigDecimal amount) {
        Periods.requireYear(year);
        Months.requireValid(month);
        var nextOrder = lines.findByYearAndMonthOrderBySortOrderAscIdAsc(year, month).stream()
                .mapToInt(RevenueLine::getSortOrder).max().orElse(0) + 1;
        return lines.save(new RevenueLine().setYear(year).setMonth(month).setDescription(description.trim())
                .setAmount(Money.requireReasonable(amount)).setSortOrder(nextOrder));
    }

    public void delete(long id) {
        lines.delete(lines.findById(id).orElseThrow(() -> new NoSuchElementException("Earning " + id + " not found")));
    }
}
