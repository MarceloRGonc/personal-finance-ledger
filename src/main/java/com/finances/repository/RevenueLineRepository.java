package com.finances.repository;

import com.finances.domain.RevenueLine;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevenueLineRepository extends JpaRepository<RevenueLine, Long> {
    List<RevenueLine> findByYearOrderByMonthAscSortOrderAscIdAsc(int year);

    List<RevenueLine> findByYearAndMonthOrderBySortOrderAscIdAsc(int year, int month);
}
