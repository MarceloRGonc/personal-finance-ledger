package com.finances.calc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.finances.IntegrationTestSupport;
import com.finances.domain.AnnualMetric;
import com.finances.domain.InvestmentAsset;
import com.finances.service.InvestmentService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class InvestmentSaleTest extends IntegrationTestSupport {

    @Autowired InvestmentService investments;
    @Autowired PortfolioService portfolio;
    @Autowired AnnualHistoryService annualHistory;

    @Test
    void soldInvestmentLosesItsCostFromInvestedAndTheProceedsBecomeCash() {
        long msci = investments.addAsset("MSCI World", "CHART_PORTFOLIO").getId();
        investments.setContribution(2026, 1, msci, new BigDecimal("100"));

        investments.recordSale(2026, 6, msci, new BigDecimal("120"), new BigDecimal("100"));

        var value = portfolio.portfolio(2026).assets().stream()
                .filter(a -> a.name().equals("MSCI World")).findFirst().orElseThrow().value();
        assertThat(value).isEqualByComparingTo("0");
        var history = annualHistory.history(2026);
        assertThat(history.rows().get(AnnualMetric.INVESTED).get(2026)).isEqualByComparingTo("0");
        assertThat(history.overview().yearly().get("ANNUAL_SALE_PROCEEDS")).isEqualByComparingTo("120");
        assertThat(history.overview().yearly().get("TOTAL_LIQUID")).isEqualByComparingTo("20");
    }

    @Test
    void deletedInvestmentDisappearsFromItsYearButKeepsEarlierValues() {
        long crypto = investments.addAsset("Crypto", "").getId();
        investments.setContribution(2026, 3, crypto, new BigDecimal("50"));

        investments.closeFrom(crypto, 2027);

        assertThat(investments.grid(2026).assets()).extracting(InvestmentAsset::getName).contains("Crypto");
        assertThat(investments.grid(2027).assets()).extracting(InvestmentAsset::getName).doesNotContain("Crypto");
        assertThat(investments.grid(2027).closedAssets()).extracting(InvestmentAsset::getName).containsExactly("Crypto");
        assertThatThrownBy(() -> investments.setContribution(2027, 1, crypto, new BigDecimal("10")))
                .isInstanceOf(IllegalArgumentException.class);

        investments.reopen(crypto);

        assertThat(investments.grid(2027).assets()).extracting(InvestmentAsset::getName).contains("Crypto");
    }

    @Test
    void investmentDeletedFromLaterYearCanStillBeSold() {
        long crypto = investments.addAsset("Crypto", "").getId();
        investments.setContribution(2026, 3, crypto, new BigDecimal("50"));
        investments.closeFrom(crypto, 2027);

        investments.recordSale(2027, 2, crypto, new BigDecimal("60"), new BigDecimal("50"));

        assertThat(investments.grid(2027).sales()).singleElement()
                .satisfies(sale -> assertThat(sale.profit()).isEqualByComparingTo("10"));
    }
}
