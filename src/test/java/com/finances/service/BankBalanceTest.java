package com.finances.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.finances.IntegrationTestSupport;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class BankBalanceTest extends IntegrationTestSupport {

    @Autowired
    BankAccountService accounts;

    @Test
    void updatingABalanceChangesOnlyThatYear() {
        var id = accounts.add("Main", new BigDecimal("100"), 2025).getId();
        accounts.updateBalance(id, 2026, new BigDecimal("200"));

        accounts.updateBalance(id, 2026, new BigDecimal("300"));

        assertThat(balanceIn(2025, id)).isEqualByComparingTo("100");
        assertThat(balanceIn(2026, id)).isEqualByComparingTo("300");
    }

    @Test
    void laterYearsKeepTheirOwnBalanceWhenAnEarlierYearChanges() {
        var id = accounts.add("Main", new BigDecimal("100"), 2025).getId();
        accounts.updateBalance(id, 2026, new BigDecimal("200"));

        accounts.updateBalance(id, 2025, new BigDecimal("150"));

        assertThat(balanceIn(2025, id)).isEqualByComparingTo("150");
        assertThat(balanceIn(2026, id)).isEqualByComparingTo("200");
    }

    @Test
    void yearsWithoutTheirOwnBalanceUseThePreviousYear() {
        var id = accounts.add("Main", new BigDecimal("100"), 2025).getId();

        assertThat(balanceIn(2027, id)).isEqualByComparingTo("100");
    }

    @Test
    void yearsBeforeTheFirstBalanceAreZero() {
        var id = accounts.add("Main", new BigDecimal("100"), 2025).getId();

        assertThat(balanceIn(2024, id)).isEqualByComparingTo("0");
    }

    private BigDecimal balanceIn(int year, long id) {
        return accounts.list(year).stream()
                .filter(account -> account.id() == id)
                .findFirst()
                .orElseThrow()
                .balance();
    }
}
