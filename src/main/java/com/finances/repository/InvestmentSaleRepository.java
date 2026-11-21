package com.finances.repository;

import com.finances.domain.InvestmentSale;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestmentSaleRepository extends JpaRepository<InvestmentSale, Long> {
    List<InvestmentSale> findByYear(int year);

    List<InvestmentSale> findByYearOrderByMonthAscIdAsc(int year);

    void deleteByAssetId(long assetId);
}
