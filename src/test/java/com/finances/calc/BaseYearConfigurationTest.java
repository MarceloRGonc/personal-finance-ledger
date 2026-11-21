package com.finances.calc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.finances.IntegrationTestSupport;
import com.finances.domain.AnnualMetric;
import com.finances.service.AnnualFigureService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "finance.base-year=2024")
class BaseYearConfigurationTest extends IntegrationTestSupport {

    @Autowired
    AnnualFigureService annualFigures;
    @Autowired
    AnnualHistoryService annualHistory;

    @Test
    void historyStartsAtTheConfiguredBaseYear() {
        var history = annualHistory.history(2026);

        assertThat(history.baseYear()).isEqualTo(2024);
        assertThat(history.years()).containsExactly(2024, 2025, 2026);
    }

    @Test
    void onlyTheConfiguredBaseYearAcceptsTypedFigures() {
        annualFigures.set(2024, AnnualMetric.NET_VALUE, new BigDecimal("100"));

        assertThatThrownBy(() -> annualFigures.set(2023, AnnualMetric.NET_VALUE, new BigDecimal("1")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("2024");
        assertThat(annualHistory.history(2025).rows().get(AnnualMetric.NET_VALUE).get(2024))
                .isEqualByComparingTo("100");
    }
}
