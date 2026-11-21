package com.finances.repository;

import com.finances.domain.RevenueValue;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevenueValueRepository extends JpaRepository<RevenueValue, Long> {
    List<RevenueValue> findByYear(int year);

    Optional<RevenueValue> findByYearAndMonthAndFieldCode(int year, int month, String fieldCode);
}
