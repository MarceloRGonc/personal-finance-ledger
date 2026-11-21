package com.finances.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class QuartersTest {

    @Test
    void theYearHasFourQuarters() {
        assertThat(Quarters.ALL).containsExactly(1, 2, 3, 4);
    }

    @Test
    void eachQuarterCoversThreeConsecutiveMonths() {
        assertThat(Quarters.months(1)).containsExactly(1, 2, 3);
        assertThat(Quarters.months(2)).containsExactly(4, 5, 6);
        assertThat(Quarters.months(4)).containsExactly(10, 11, 12);
    }

    @Test
    void aMonthBelongsToTheQuarterThatContainsIt() {
        assertThat(Quarters.ofMonth(1)).isEqualTo(1);
        assertThat(Quarters.ofMonth(3)).isEqualTo(1);
        assertThat(Quarters.ofMonth(4)).isEqualTo(2);
        assertThat(Quarters.ofMonth(11)).isEqualTo(4);
    }

    @Test
    void theCurrentYearOpensOnTheCurrentQuarterAndOtherYearsOnTheFirst() {
        var november = LocalDate.of(2026, 11, 21);

        assertThat(Quarters.defaultFor(2026, november)).isEqualTo(4);
        assertThat(Quarters.defaultFor(2025, november)).isEqualTo(1);
        assertThat(Quarters.defaultFor(2026, LocalDate.of(2026, 1, 5))).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 5, -1})
    void quartersOutsideTheYearAreRejected(int quarter) {
        assertThatThrownBy(() -> Quarters.months(quarter))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("between 1 and 4");
    }
}
