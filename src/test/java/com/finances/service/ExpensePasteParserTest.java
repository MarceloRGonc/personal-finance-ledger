package com.finances.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ExpensePasteParserTest {

    @Test
    void readsTabSeparatedRowsCopiedFromExcel() {
        var rows = ExpensePasteParser.parse("Rent\t1200,00\nGroceries\t345.5");

        assertThat(rows).extracting(PastedExpense::description).containsExactly("Rent", "Groceries");
        assertThat(rows).extracting(PastedExpense::amount)
                .usingElementComparator(BigDecimal::compareTo)
                .containsExactly(new BigDecimal("1200"), new BigDecimal("345.5"));
    }

    @Test
    void acceptsSemicolonsAndDescriptionsWithSpaces() {
        var rows = ExpensePasteParser.parse("Phone bill; 19,99\nCoffee with friends 2.50");

        assertThat(rows).extracting(PastedExpense::description).containsExactly("Phone bill", "Coffee with friends");
    }

    @Test
    void skipsBlankLinesAndAHeaderRow() {
        var rows = ExpensePasteParser.parse("Description\tAmount\n\n   \nRent\t10");

        assertThat(rows).extracting(PastedExpense::description).containsExactly("Rent");
    }

    @Test
    void handlesWindowsLineEndings() {
        assertThat(ExpensePasteParser.parse("Rent\t10\r\nFood\t5")).hasSize(2);
    }

    @Test
    void reportsEveryBadLineWithItsNumber() {
        assertThatThrownBy(() -> ExpensePasteParser.parse("Rent\tabc\n\t5\nOk\t3\nNoAmount"))
                .isInstanceOfSatisfying(ExpensePasteException.class, e -> assertThat(e.errors()).containsExactly(
                        "Line 1: amount 'abc' is not a number",
                        "Line 2: description is missing",
                        "Line 4: expected a description and an amount"));
    }

    @Test
    void rejectsAmountsOutOfRange() {
        assertThatThrownBy(() -> ExpensePasteParser.parse("Huge\t99999999999999"))
                .isInstanceOfSatisfying(ExpensePasteException.class,
                        e -> assertThat(e.errors()).containsExactly("Line 1: amount is out of range"));
    }

    @Test
    void rejectsAPasteWithNoRows() {
        assertThatThrownBy(() -> ExpensePasteParser.parse("  \n\n"))
                .isInstanceOfSatisfying(ExpensePasteException.class,
                        e -> assertThat(e.errors()).containsExactly("Paste at least one expense"));
    }
}
