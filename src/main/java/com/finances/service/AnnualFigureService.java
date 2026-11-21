package com.finances.service;

import com.finances.common.Money;
import com.finances.config.FinanceProperties;
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
    private final int baseYear;

    public AnnualFigureService(AnnualFigureRepository figures, FinanceProperties properties) {
        this.figures = figures;
        this.baseYear = properties.baseYear();
    }

    public void set(int year, AnnualMetric metric, BigDecimal amount) {
        if (year != baseYear) {
            throw new IllegalArgumentException("Only " + baseYear + " is typed in; later years are calculated");
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
