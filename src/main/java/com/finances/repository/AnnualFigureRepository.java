package com.finances.repository;

import com.finances.domain.AnnualFigure;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnualFigureRepository extends JpaRepository<AnnualFigure, Long> {
    List<AnnualFigure> findByYear(int year);

    Optional<AnnualFigure> findByYearAndMetric(int year, String metric);
}
