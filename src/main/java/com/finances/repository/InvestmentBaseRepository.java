package com.finances.repository;

import com.finances.domain.InvestmentBase;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestmentBaseRepository extends JpaRepository<InvestmentBase, Long> {
    List<InvestmentBase> findByYear(int year);

    Optional<InvestmentBase> findByYearAndAssetId(int year, long assetId);

    void deleteByAssetId(long assetId);
}
