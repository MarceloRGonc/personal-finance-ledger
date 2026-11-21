package com.finances.service;

import com.finances.domain.Expense;
import java.math.BigDecimal;
import java.util.List;

public record ExpenseGrid(int year, int quarter, List<MonthColumn> columns) {

    public record MonthColumn(int month, String label, List<Expense> items, BigDecimal total) {

        public Expense itemAt(int row) {
            return row < items.size() ? items.get(row) : null;
        }
    }

    public int rowCount() {
        return columns.stream().mapToInt(c -> c.items().size()).max().orElse(0);
    }
}
