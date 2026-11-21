package com.finances.calc;

import static org.assertj.core.api.Assertions.assertThat;

import com.finances.IntegrationTestSupport;
import com.finances.domain.AnnualMetric;
import com.finances.service.AnnualFigureService;
import com.finances.service.InvestmentService;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class YearOverviewServiceTest extends IntegrationTestSupport {

    @Autowired YearOverviewService overviews;
    @Autowired PortfolioService portfolio;
    @Autowired LoanService loans;
    @Autowired InvestmentService investments;
    @Autowired AnnualHistoryService annualHistory;
    @Autowired AnnualFigureService annualFigures;

    @Test
    void computesMonthlyRollupFromStoredRulesIncludingEarningLines() {
        loadSampleLedger();

        var january = overviews.overview(2026).months().get(0).values();

        assertThat(january.get("GROSS_REVENUE")).isEqualByComparingTo("1100");
        assertThat(january.get("NET_REVENUE")).isEqualByComparingTo("975");
        assertThat(january.get("INVESTMENTS")).isEqualByComparingTo("180");
        assertThat(january.get("EXPENSES")).isEqualByComparingTo("300");
        assertThat(january.get("MONTH_BALANCE")).isEqualByComparingTo("495");
    }

    @Test
    void computesYearlyFiguresUsingPreviousYearAndBankBalances() {
        loadSampleLedger();

        var overview = overviews.overview(2026, Map.of("NET_VALUE", new BigDecimal("200"), "INVESTED", new BigDecimal("60")));

        assertThat(overview.hasData()).isTrue();
        assertThat(overview.yearly().get("NET_ANNUAL_INCOME")).isEqualByComparingTo("996");
        assertThat(overview.yearly().get("TOTAL_LIQUID")).isEqualByComparingTo("671");
        assertThat(overview.yearly().get("CURRENT_BALANCE")).isEqualByComparingTo("916");
        assertThat(overview.yearly().get("BANK_TOTAL")).isEqualByComparingTo("150");
        assertThat(overview.yearly().get("BANK_DIFFERENCE")).isEqualByComparingTo("-521");
    }

    @Test
    void valuesInvestmentsAndLoansAndBuildsChartData() {
        loadSampleLedger();

        var view = portfolio.portfolio(2026);

        assertThat(view.loansRemaining()).isEqualByComparingTo("600");
        assertThat(view.totals().get("INVESTED_TOTAL")).isEqualByComparingTo("11285");
        assertThat(view.totals().get("ETF_SUBTOTAL")).isEqualByComparingTo("515");
        assertThat(portfolio.chart(2026, PortfolioService.ETF_CHART).labels()).containsExactly("S&P 500");
        assertThat(portfolio.chart(2026, PortfolioService.PORTFOLIO_CHART).labels()).containsExactly("S&P 500", "Pension Plan");
    }

    @Test
    void loanRemainingUsesOpeningBalanceMinusCountedPayments() {
        loadSampleLedger();

        assertThat(loans.summaries()).singleElement().satisfies(loan -> {
            assertThat(loan.principal()).isEqualByComparingTo("1000");
            assertThat(loan.remaining()).isEqualByComparingTo("600");
        });
    }

    @Test
    void investmentValueCarriesForwardFromPreviousYearWhenNotStoredExplicitly() {
        var asset = investments.addAsset("Test Fund", "CHART_PORTFOLIO");
        investments.setOpeningValue(2026, asset.getId(), new BigDecimal("100"));
        investments.setContribution(2026, 3, asset.getId(), new BigDecimal("25"));

        var next = portfolio.portfolio(2027).assets().get(0);

        assertThat(next.baseAmount()).isEqualByComparingTo("125");
        assertThat(next.value()).isEqualByComparingTo("125");
    }

    @Test
    void yearsWithoutMonthlyDataCarryTheBaseYearFiguresForward() {
        annualFigures.set(2023, AnnualMetric.NET_VALUE, new BigDecimal("100"));
        annualFigures.set(2023, AnnualMetric.INVESTED, new BigDecimal("50"));

        var comparisons = annualHistory.comparisons(annualHistory.history(2025));
        var y2024 = comparisons.stream().filter(c -> c.year() == 2024).findFirst().orElseThrow();

        assertThat(y2024.values().get(AnnualMetric.NET_VALUE)).isEqualByComparingTo("100");
        assertThat(y2024.values().get(AnnualMetric.INVESTED)).isEqualByComparingTo("50");
        assertThat(y2024.change().get(AnnualMetric.NET_VALUE)).isEqualByComparingTo("0");
    }

    @Test
    void selectedYearStartsFromTheCarriedForwardNetValue() {
        annualFigures.set(2023, AnnualMetric.NET_VALUE, new BigDecimal("100"));
        loadSampleLedger();

        var overview = annualHistory.history(2026).overview();

        assertThat(overview.yearly().get("TOTAL_LIQUID")).isEqualByComparingTo("571");
    }
}
