package com.finances.service;

import com.finances.calc.PortfolioService;
import com.finances.common.Money;
import com.finances.common.Months;
import com.finances.common.Periods;
import com.finances.domain.InvestmentAsset;
import com.finances.domain.InvestmentBase;
import com.finances.domain.InvestmentContribution;
import com.finances.domain.InvestmentSale;
import com.finances.repository.InvestmentAssetRepository;
import com.finances.repository.InvestmentBaseRepository;
import com.finances.repository.InvestmentContributionRepository;
import com.finances.repository.InvestmentSaleRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InvestmentService {

    private static final int MAX_TAGS_LENGTH = 200;

    private final InvestmentAssetRepository assets;
    private final InvestmentBaseRepository bases;
    private final InvestmentContributionRepository contributions;
    private final InvestmentSaleRepository sales;
    private final PortfolioService portfolio;

    public InvestmentService(InvestmentAssetRepository assets, InvestmentBaseRepository bases,
                             InvestmentContributionRepository contributions, InvestmentSaleRepository sales,
                             PortfolioService portfolio) {
        this.assets = assets;
        this.bases = bases;
        this.contributions = contributions;
        this.sales = sales;
        this.portfolio = portfolio;
    }

    @Transactional(readOnly = true)
    public InvestmentGrid grid(int year) {
        var byMonth = new HashMap<Integer, Map<Long, BigDecimal>>();
        var totals = new HashMap<Long, BigDecimal>();
        for (var contribution : contributions.findByYear(year)) {
            byMonth.computeIfAbsent(contribution.getMonth(), m -> new HashMap<>())
                    .put(contribution.getAssetId(), contribution.getAmount());
            totals.merge(contribution.getAssetId(), contribution.getAmount(), BigDecimal::add);
        }
        var opening = new HashMap<Long, BigDecimal>();
        portfolio.portfolio(year).assets().forEach(a -> opening.put(a.id(), a.baseAmount()));

        var allAssets = assets.findAllByOrderBySortOrderAscIdAsc();
        var names = new HashMap<Long, String>();
        allAssets.forEach(a -> names.put(a.getId(), a.getName()));
        var visible = allAssets.stream().filter(a -> a.isOpenIn(year)).toList();
        var deleted = allAssets.stream().filter(a -> a.getClosedFromYear() != null).toList();
        var saleRows = sales.findByYearOrderByMonthAscIdAsc(year).stream()
                .map(sale -> SaleView.of(sale, names.getOrDefault(sale.getAssetId(), "#" + sale.getAssetId())))
                .toList();
        return new InvestmentGrid(year, visible, deleted, opening, byMonth, totals,
                Money.sum(totals.values().stream().toList()), saleRows,
                Money.sum(saleRows.stream().map(SaleView::proceeds).toList()),
                Money.sum(saleRows.stream().map(SaleView::cost).toList()),
                Money.sum(saleRows.stream().map(SaleView::profit).toList()));
    }

    public void setContribution(int year, int month, long assetId, BigDecimal amount) {
        Periods.requireYear(year);
        Months.requireValid(month);
        var asset = requireAsset(assetId);
        var existing = contributions.findByYearAndMonthAndAssetId(year, month, assetId);
        if (amount == null) {
            existing.ifPresent(contributions::delete);
            return;
        }
        requireOpen(asset, year);
        var entity = existing.orElseGet(() ->
                new InvestmentContribution().setYear(year).setMonth(month).setAssetId(assetId));
        contributions.save(entity.setAmount(Money.requireReasonable(amount)));
    }

    public void setOpeningValue(int year, long assetId, BigDecimal amount) {
        Periods.requireYear(year);
        var asset = requireAsset(assetId);
        var existing = bases.findByYearAndAssetId(year, assetId);
        if (amount == null) {
            existing.ifPresent(bases::delete);
            return;
        }
        requireOpen(asset, year);
        var entity = existing.orElseGet(() -> new InvestmentBase().setYear(year).setAssetId(assetId));
        bases.save(entity.setAmount(Money.requireReasonable(amount)));
    }

    public InvestmentAsset addAsset(String name, String tags) {
        var trimmed = name.trim();
        if (assets.findByNameIgnoreCase(trimmed).isPresent()) {
            throw new IllegalArgumentException("An investment named " + trimmed + " already exists");
        }
        var nextOrder = assets.findAllByOrderBySortOrderAscIdAsc().stream()
                .mapToInt(InvestmentAsset::getSortOrder).max().orElse(0) + 1;
        return assets.save(new InvestmentAsset().setName(trimmed).setTags(normalizedTags(tags))
                .setSortOrder(nextOrder));
    }

    public void updateTags(long assetId, String tags) {
        requireAsset(assetId).setTags(normalizedTags(tags));
    }

    private static String normalizedTags(String tags) {
        var trimmed = tags == null ? "" : tags.trim();
        if (trimmed.length() > MAX_TAGS_LENGTH) {
            throw new IllegalArgumentException("Tags must be at most " + MAX_TAGS_LENGTH + " characters");
        }
        return trimmed;
    }

    /** Stops the investment's column from {@code year} on. Its value stays in the portfolio until it is sold. */
    public void closeFrom(long assetId, int year) {
        Periods.requireYear(year);
        requireAsset(assetId).setClosedFromYear(year);
    }

    public void reopen(long assetId) {
        requireAsset(assetId).setClosedFromYear(null);
    }

    /** Records a sale. The proceeds become cash, and the cost is removed from the invested amount. */
    public SaleView recordSale(int year, int month, long assetId, BigDecimal proceeds, BigDecimal cost) {
        Periods.requireYear(year);
        Months.requireValid(month);
        var asset = requireAsset(assetId);
        var sale = sales.save(new InvestmentSale().setYear(year).setMonth(month).setAssetId(assetId)
                .setProceeds(requireNonNegative(proceeds)).setCost(requireNonNegative(cost)));
        return SaleView.of(sale, asset.getName());
    }

    public void deleteSale(long saleId) {
        sales.delete(sales.findById(saleId)
                .orElseThrow(() -> new NoSuchElementException("Sale " + saleId + " not found")));
    }

    /** Permanently removes the investment with all its contributions, openings and sales. */
    public void removeAsset(long assetId) {
        var asset = requireAsset(assetId);
        contributions.deleteByAssetId(assetId);
        bases.deleteByAssetId(assetId);
        sales.deleteByAssetId(assetId);
        assets.delete(asset);
    }

    public void moveUp(long assetId) {
        move(assetId, -1);
    }

    public void moveDown(long assetId) {
        move(assetId, 1);
    }

    private void move(long assetId, int offset) {
        requireAsset(assetId);
        var ordered = new ArrayList<>(assets.findAllByOrderBySortOrderAscIdAsc());
        var position = ordered.stream().map(InvestmentAsset::getId).toList().indexOf(assetId);
        var target = position + offset;
        if (target < 0 || target >= ordered.size()) {
            return;
        }
        Collections.swap(ordered, position, target);
        for (var index = 0; index < ordered.size(); index++) {
            ordered.get(index).setSortOrder(index + 1);
        }
    }

    private InvestmentAsset requireAsset(long assetId) {
        return assets.findById(assetId).orElseThrow(() -> new NoSuchElementException("Investment " + assetId + " not found"));
    }

    private static void requireOpen(InvestmentAsset asset, int year) {
        if (!asset.isOpenIn(year)) {
            throw new IllegalArgumentException(asset.getName() + " was deleted from " + asset.getClosedFromYear()
                    + ". Reopen it to add values for " + year);
        }
    }

    private static BigDecimal requireNonNegative(BigDecimal amount) {
        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException("Amount must be zero or more");
        }
        return Money.requireReasonable(amount);
    }
}
