package com.finances.service;

import com.finances.domain.InvestmentAsset;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record InvestmentGrid(int year, List<InvestmentAsset> assets, List<InvestmentAsset> closedAssets,
                             Map<Integer, Map<Long, BigDecimal>> byMonth, Map<Integer, BigDecimal> monthTotals,
                             Map<Long, BigDecimal> totals, BigDecimal grandTotal, List<SaleView> sales,
                             BigDecimal soldProceeds, BigDecimal soldCost, BigDecimal realizedProfit) {
}
