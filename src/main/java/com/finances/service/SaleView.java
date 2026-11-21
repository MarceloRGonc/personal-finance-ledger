package com.finances.service;

import com.finances.domain.InvestmentSale;
import java.math.BigDecimal;

public record SaleView(Long id, int month, String assetName, BigDecimal proceeds, BigDecimal cost, BigDecimal profit) {

    public static SaleView of(InvestmentSale sale, String assetName) {
        return new SaleView(sale.getId(), sale.getMonth(), assetName, sale.getProceeds(), sale.getCost(),
                sale.getProceeds().subtract(sale.getCost()));
    }
}
