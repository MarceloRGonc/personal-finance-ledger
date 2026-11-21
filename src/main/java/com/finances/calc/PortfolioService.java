package com.finances.calc;

import com.finances.common.DefaultingMap;
import com.finances.common.Money;
import com.finances.domain.FormulaScope;
import com.finances.domain.InvestmentAsset;
import com.finances.domain.InvestmentBase;
import com.finances.domain.InvestmentContribution;
import com.finances.domain.InvestmentSale;
import com.finances.formula.FormulaService;
import com.finances.repository.InvestmentAssetRepository;
import com.finances.repository.InvestmentBaseRepository;
import com.finances.repository.InvestmentContributionRepository;
import com.finances.repository.InvestmentSaleRepository;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PortfolioService {

    public static final String PORTFOLIO_CHART = "CHART_PORTFOLIO";
    public static final String ETF_CHART = "CHART_ETFS";

    private final InvestmentAssetRepository assets;
    private final InvestmentBaseRepository bases;
    private final InvestmentContributionRepository contributions;
    private final InvestmentSaleRepository sales;
    private final LoanService loans;
    private final FormulaService formulas;

    public PortfolioService(InvestmentAssetRepository assets, InvestmentBaseRepository bases,
                            InvestmentContributionRepository contributions, InvestmentSaleRepository sales,
                            LoanService loans, FormulaService formulas) {
        this.assets = assets;
        this.bases = bases;
        this.contributions = contributions;
        this.sales = sales;
        this.loans = loans;
        this.formulas = formulas;
    }

    public PortfolioView portfolio(int year) {
        var ledger = new Ledger(bases.findAll(), contributions.findAll(), sales.findAll());
        var loansRemaining = loans.remainingTotal();
        var views = assets.findAllByOrderBySortOrderAscIdAsc().stream()
                .map(asset -> view(asset, year, ledger))
                .toList();

        var byName = new DefaultingMap();
        var byTag = new DefaultingMap();
        views.forEach(view -> {
            byName.put(view.name(), view.value());
            view.tags().forEach(tag -> byTag.merge(tag, view.value(), BigDecimal::add));
        });
        Map<String, Object> variables = new HashMap<>();
        variables.put("holdings", byName);
        variables.put("holdingsTotal", Money.sum(views.stream().map(AssetView::value).toList()));
        variables.put("loansRemaining", loansRemaining);
        variables.put("tags", byTag);
        return new PortfolioView(views, loansRemaining, formulas.evaluate(FormulaScope.PORTFOLIO, variables));
    }

    public ChartData chart(int year, String tag) {
        var tagged = portfolio(year).assets().stream()
                .filter(a -> a.tags().contains(tag) && a.value().signum() > 0)
                .toList();
        return new ChartData(tagged.stream().map(AssetView::name).toList(),
                tagged.stream().map(AssetView::value).toList());
    }

    private static AssetView view(InvestmentAsset asset, int year, Ledger ledger) {
        var base = ledger.base(asset.getId(), year);
        var contributed = ledger.contribution(asset.getId(), year);
        var sold = ledger.sold(asset.getId(), year);
        var value = base.add(contributed).subtract(sold);
        return new AssetView(asset.getId(), asset.getName(), base, contributed, sold, value, asset.tagSet());
    }

    /**
     * Opening value of an asset for a year. An explicitly stored base wins; otherwise the value carries
     * forward from the previous year (its base plus that year's contributions), so 2024 follows 2023,
     * 2025 follows 2024 and so on.
     */
    private static final class Ledger {

        private final Map<Integer, Map<Long, BigDecimal>> explicitBases;
        private final Map<Integer, Map<Long, BigDecimal>> yearlyContributions;
        private final Map<Integer, Map<Long, BigDecimal>> yearlySoldCost;
        private final int earliestBaseYear;

        Ledger(List<InvestmentBase> baseRows, List<InvestmentContribution> contributionRows, List<InvestmentSale> saleRows) {
            this.explicitBases = baseRows.stream().collect(Collectors.groupingBy(InvestmentBase::getYear,
                    Collectors.toMap(InvestmentBase::getAssetId, InvestmentBase::getAmount, BigDecimal::add)));
            this.yearlyContributions = contributionRows.stream().collect(Collectors.groupingBy(InvestmentContribution::getYear,
                    Collectors.groupingBy(InvestmentContribution::getAssetId,
                            Collectors.reducing(BigDecimal.ZERO, InvestmentContribution::getAmount, BigDecimal::add))));
            this.yearlySoldCost = saleRows.stream().collect(Collectors.groupingBy(InvestmentSale::getYear,
                    Collectors.groupingBy(InvestmentSale::getAssetId,
                            Collectors.reducing(BigDecimal.ZERO, InvestmentSale::getCost, BigDecimal::add))));
            this.earliestBaseYear = explicitBases.keySet().stream().mapToInt(Integer::intValue).min()
                    .orElse(Integer.MAX_VALUE);
        }

        BigDecimal base(long assetId, int year) {
            var explicit = explicitBases.getOrDefault(year, Map.of()).get(assetId);
            if (explicit != null) {
                return explicit;
            }
            if (year <= earliestBaseYear) {
                return BigDecimal.ZERO;
            }
            return base(assetId, year - 1).add(contribution(assetId, year - 1)).subtract(sold(assetId, year - 1));
        }

        BigDecimal contribution(long assetId, int year) {
            return yearlyContributions.getOrDefault(year, Map.of()).getOrDefault(assetId, BigDecimal.ZERO);
        }

        BigDecimal sold(long assetId, int year) {
            return yearlySoldCost.getOrDefault(year, Map.of()).getOrDefault(assetId, BigDecimal.ZERO);
        }
    }
}
