package com.finances.repository;

import com.finances.domain.InvestmentAsset;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestmentAssetRepository extends JpaRepository<InvestmentAsset, Long> {
    List<InvestmentAsset> findAllByOrderBySortOrderAscIdAsc();

    Optional<InvestmentAsset> findByNameIgnoreCase(String name);
}
