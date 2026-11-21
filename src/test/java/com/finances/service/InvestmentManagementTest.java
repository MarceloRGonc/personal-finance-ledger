package com.finances.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.finances.IntegrationTestSupport;
import com.finances.domain.InvestmentAsset;
import com.finances.repository.InvestmentAssetRepository;
import com.finances.repository.InvestmentBaseRepository;
import com.finances.repository.InvestmentContributionRepository;
import com.finances.repository.InvestmentSaleRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class InvestmentManagementTest extends IntegrationTestSupport {

    @Autowired
    InvestmentService investments;
    @Autowired
    InvestmentAssetRepository assets;
    @Autowired
    InvestmentContributionRepository contributions;
    @Autowired
    InvestmentBaseRepository bases;
    @Autowired
    InvestmentSaleRepository sales;

    @Test
    void removingAnInvestmentDeletesItsWholeHistory() {
        var id = investments.addAsset("Crypto", "").getId();
        investments.setContribution(2026, 1, id, new BigDecimal("100"));
        investments.setOpeningValue(2026, id, new BigDecimal("50"));
        investments.recordSale(2026, 2, id, new BigDecimal("120"), new BigDecimal("100"));

        investments.removeAsset(id);

        assertThat(assets.findById(id)).isEmpty();
        assertThat(contributions.findByYear(2026)).isEmpty();
        assertThat(bases.findByYear(2026)).isEmpty();
        assertThat(sales.findByYear(2026)).isEmpty();
    }

    @Test
    void investmentsCanBeMovedUpAndDown() {
        var a = investments.addAsset("A", "").getId();
        investments.addAsset("B", "");
        var c = investments.addAsset("C", "").getId();

        investments.moveDown(a);
        assertThat(names()).containsExactly("B", "A", "C");

        investments.moveUp(c);
        assertThat(names()).containsExactly("B", "C", "A");
    }

    @Test
    void movingTheFirstInvestmentUpChangesNothing() {
        var a = investments.addAsset("A", "").getId();
        investments.addAsset("B", "");

        investments.moveUp(a);

        assertThat(names()).containsExactly("A", "B");
    }

    @Test
    void tagsLongerThanTwoHundredCharactersAreRejectedWhenAddingAndUpdating() {
        var id = investments.addAsset("A", "").getId();
        var tooLong = "X".repeat(201);

        assertThatThrownBy(() -> investments.addAsset("B", tooLong))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("200");
        assertThatThrownBy(() -> investments.updateTags(id, tooLong))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("200");
        assertThat(assets.findByNameIgnoreCase("B")).isEmpty();
    }

    @Test
    void tagsAreTrimmed() {
        var id = investments.addAsset("A", "  CHART_PORTFOLIO  ").getId();

        investments.updateTags(id, "  CHART_ETFS ");

        assertThat(assets.findById(id)).get().satisfies(asset -> assertThat(asset.getTags()).isEqualTo("CHART_ETFS"));
    }

    @Test
    void gridTotalsEachMonthAcrossAllInvestments() {
        var a = investments.addAsset("A", "").getId();
        var b = investments.addAsset("B", "").getId();
        investments.setContribution(2026, 4, a, new BigDecimal("10"));
        investments.setContribution(2026, 4, b, new BigDecimal("5.50"));
        investments.setContribution(2026, 5, a, new BigDecimal("1"));

        var grid = investments.grid(2026);

        assertThat(grid.monthTotals().get(4)).isEqualByComparingTo("15.50");
        assertThat(grid.monthTotals().get(5)).isEqualByComparingTo("1");
        assertThat(grid.monthTotals()).doesNotContainKey(6);
    }

    private List<String> names() {
        return assets.findAllByOrderBySortOrderAscIdAsc().stream().map(InvestmentAsset::getName).toList();
    }
}
