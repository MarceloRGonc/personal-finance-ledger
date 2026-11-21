package com.finances.service;

import com.finances.common.Money;
import com.finances.common.Periods;
import com.finances.domain.AnnualFigure;
import com.finances.domain.AnnualMetric;
import com.finances.repository.AnnualFigureRepository;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AnnualFigureService {

    private final AnnualFigureRepository figures;

    public AnnualFigureService(AnnualFigureRepository figures) {
        this.figures = figures;
    }

    public void set(int year, AnnualMetric metric, BigDecimal amount) {
        if (year != Periods.BASE_YEAR) {
            throw new IllegalArgumentException("Only " + Periods.BASE_YEAR + " is typed in; later years are calculated");
        }
        var existing = figures.findByYearAndMetric(year, metric.name());
        if (amount == null) {
            existing.ifPresent(figures::delete);
            return;
        }
        var entity = existing.orElseGet(() -> new AnnualFigure().setYear(year).setMetric(metric.name()));
        figures.save(entity.setAmount(Money.requireReasonable(amount)));
    }
}
