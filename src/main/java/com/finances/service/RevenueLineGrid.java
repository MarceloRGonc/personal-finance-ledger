package com.finances.service;

import com.finances.domain.RevenueLine;
import java.math.BigDecimal;
import java.util.List;

public record RevenueLineGrid(int year, int quarter, List<MonthColumn> columns) {

    public record MonthColumn(int month, String label, List<RevenueLine> items, BigDecimal total) {

        public RevenueLine itemAt(int row) {
            return row < items.size() ? items.get(row) : null;
        }
    }

    public int rowCount() {
        return columns.stream().mapToInt(c -> c.items().size()).max().orElse(0);
    }
}
