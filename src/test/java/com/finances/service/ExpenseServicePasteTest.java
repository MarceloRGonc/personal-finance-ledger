package com.finances.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.finances.IntegrationTestSupport;
import com.finances.domain.Expense;
import com.finances.repository.ExpenseRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ExpenseServicePasteTest extends IntegrationTestSupport {

    @Autowired
    ExpenseService expenses;
    @Autowired
    ExpenseRepository repository;

    @Test
    void pastedRowsAreAddedToTheMonthInOrder() {
        var saved = expenses.paste(2026, 3, "Rent\t1200\nGroceries\t345,50", false);

        assertThat(saved).isEqualTo(2);
        var rows = repository.findByYearAndMonthOrderBySortOrderAscIdAsc(2026, 3);
        assertThat(rows).extracting(Expense::getDescription).containsExactly("Rent", "Groceries");
        assertThat(rows).extracting(Expense::getSortOrder).containsExactly(1, 2);
    }

    @Test
    void appendsByDefaultAndReplacesTheMonthWhenAsked() {
        expenses.add(2026, 3, "Old", new BigDecimal("1"));

        expenses.paste(2026, 3, "Rent\t1200", false);
        assertThat(descriptionsIn(3)).containsExactly("Old", "Rent");

        expenses.paste(2026, 3, "Only\t5", true);
        assertThat(descriptionsIn(3)).containsExactly("Only");
    }

    @Test
    void aPasteWithAnErrorSavesNothing() {
        assertThatThrownBy(() -> expenses.paste(2026, 3, "Ok\t1\nBad\tx", false))
                .isInstanceOf(ExpensePasteException.class);

        assertThat(repository.findByYearAndMonthOrderBySortOrderAscIdAsc(2026, 3)).isEmpty();
    }

    @Test
    void previewReturnsTheParsedRowsWithoutSavingThem() {
        var rows = expenses.preview("Rent\t1200\nFood\t5");

        assertThat(rows).extracting(PastedExpense::description).containsExactly("Rent", "Food");
        assertThat(repository.count()).isZero();
    }

    @Test
    void previewRejectsBadLinesWithoutSavingThem() {
        assertThatThrownBy(() -> expenses.preview("Bad\tx"))
                .isInstanceOf(ExpensePasteException.class);
        assertThat(repository.count()).isZero();
    }

    private List<String> descriptionsIn(int month) {
        return repository.findByYearAndMonthOrderBySortOrderAscIdAsc(2026, month).stream()
                .map(Expense::getDescription).toList();
    }
}
