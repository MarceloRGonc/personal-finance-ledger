package com.finances.repository;

import com.finances.domain.InvestmentContribution;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestmentContributionRepository extends JpaRepository<InvestmentContribution, Long> {
    List<InvestmentContribution> findByYear(int year);

    Optional<InvestmentContribution> findByYearAndMonthAndAssetId(int year, int month, long assetId);

    void deleteByAssetId(long assetId);
}
