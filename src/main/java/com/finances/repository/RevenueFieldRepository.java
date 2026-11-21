package com.finances.repository;

import com.finances.domain.RevenueField;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevenueFieldRepository extends JpaRepository<RevenueField, Long> {
    List<RevenueField> findAllByOrderBySortOrderAscIdAsc();

    Optional<RevenueField> findByCode(String code);
}
