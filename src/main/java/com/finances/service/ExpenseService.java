package com.finances.service;

import com.finances.common.Money;
import com.finances.common.Months;
import com.finances.common.Periods;
import com.finances.domain.Expense;
import com.finances.repository.ExpenseRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ExpenseService {

    public static final int QUARTERS = 4;
    private static final int MONTHS_PER_QUARTER = 3;

    private final ExpenseRepository expenses;

    public ExpenseService(ExpenseRepository expenses) {
        this.expenses = expenses;
    }

    @Transactional(readOnly = true)
    public ExpenseGrid grid(int year, int quarter) {
        if (quarter < 1 || quarter > QUARTERS) {
            throw new IllegalArgumentException("Quarter must be between 1 and " + QUARTERS);
        }
        var first = (quarter - 1) * MONTHS_PER_QUARTER;
        var columns = Months.ALL.subList(first, first + MONTHS_PER_QUARTER).stream().map(month -> {
            var items = expenses.findByYearAndMonthOrderBySortOrderAscIdAsc(year, month);
            var total = items.stream().filter(Expense::isCounted).map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return new ExpenseGrid.MonthColumn(month, Months.label(month), items, total);
        }).toList();
        return new ExpenseGrid(year, quarter, columns);
    }

    public Expense add(int year, int month, String description, BigDecimal amount) {
        Periods.requireYear(year);
        Months.requireValid(month);
        var nextOrder = expenses.findByYearAndMonthOrderBySortOrderAscIdAsc(year, month).stream()
                .mapToInt(Expense::getSortOrder).max().orElse(0) + 1;
        return expenses.save(new Expense().setYear(year).setMonth(month).setDescription(description.trim())
                .setAmount(Money.requireReasonable(amount)).setSortOrder(nextOrder));
    }

    public List<PastedExpense> preview(String text) {
        return ExpensePasteParser.parse(text);
    }

    public int paste(int year, int month, String text, boolean replace) {
        Periods.requireYear(year);
        Months.requireValid(month);
        var rows = ExpensePasteParser.parse(text);
        var existing = expenses.findByYearAndMonthOrderBySortOrderAscIdAsc(year, month);
        if (replace) {
            expenses.deleteAll(existing);
        }
        var order = replace ? 0 : existing.stream().mapToInt(Expense::getSortOrder).max().orElse(0);
        for (var row : rows) {
            order++;
            expenses.save(new Expense().setYear(year).setMonth(month).setDescription(row.description())
                    .setAmount(row.amount()).setSortOrder(order));
        }
        return rows.size();
    }

    public void delete(long id) {
        expenses.delete(find(id));
    }

    public void toggleCounted(long id) {
        var expense = find(id);
        expense.setCounted(!expense.isCounted());
    }

    private Expense find(long id) {
        return expenses.findById(id).orElseThrow(() -> new NoSuchElementException("Expense " + id + " not found"));
    }
}
