package com.finances.repository;

import com.finances.domain.Expense;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByYearOrderByMonthAscSortOrderAscIdAsc(int year);

    List<Expense> findByYearAndMonthOrderBySortOrderAscIdAsc(int year, int month);
}
