package com.finances.calc;

import com.finances.common.Periods;
import com.finances.domain.AnnualFigure;
import com.finances.domain.AnnualMetric;
import com.finances.repository.AnnualFigureRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Builds the year-by-year history. The base year uses its typed figures. Each later year starts from the
 * previous year's Net Value and Invested, so a year without monthly data keeps those values unchanged.
 */
@Service
@Transactional(readOnly = true)
public class AnnualHistoryService {

    private static final List<AnnualMetric> ANNUAL_ACTIVITY = List.of(
            AnnualMetric.GROSS_ANNUAL_INCOME, AnnualMetric.NET_ANNUAL_INCOME, AnnualMetric.ANNUAL_INVESTED);

    private final AnnualFigureRepository figures;
    private final YearOverviewService overviews;

    public AnnualHistoryService(AnnualFigureRepository figures, YearOverviewService overviews) {
        this.figures = figures;
        this.overviews = overviews;
    }

    public AnnualHistory history(int selectedYear) {
        var last = Math.max(selectedYear, Periods.BASE_YEAR);
        var years = new ArrayList<Integer>();
        var rows = new EnumMap<AnnualMetric, Map<Integer, BigDecimal>>(AnnualMetric.class);
        for (var metric : AnnualMetric.values()) {
            rows.put(metric, new HashMap<>());
        }
        Map<String, BigDecimal> previous = Map.of();
        YearOverview selected = null;
        for (int year = Periods.BASE_YEAR; year <= last; year++) {
            var overview = overviews.overview(year, previous);
            var values = year == Periods.BASE_YEAR
                    ? typedFigures(year)
                    : calculatedFigures(overview, previous);
            final int current = year;
            values.forEach((metric, value) -> rows.get(metric).put(current, value));
            years.add(year);
            previous = byName(values);
            if (year == selectedYear) {
                selected = overview;
            }
        }
        var overview = selected != null ? selected : overviews.overview(selectedYear, Map.of());
        return new AnnualHistory(Periods.BASE_YEAR, List.copyOf(years), rows, overview);
    }

    public List<YearComparison> comparisons(AnnualHistory history) {
        var result = new ArrayList<YearComparison>();
        Map<AnnualMetric, BigDecimal> previous = Map.of();
        for (int year : history.years()) {
            var values = new EnumMap<AnnualMetric, BigDecimal>(AnnualMetric.class);
            var change = new EnumMap<AnnualMetric, BigDecimal>(AnnualMetric.class);
            var percent = new EnumMap<AnnualMetric, BigDecimal>(AnnualMetric.class);
            for (var metric : AnnualMetric.values()) {
                var value = history.rows().get(metric).get(year);
                if (value == null) {
                    continue;
                }
                values.put(metric, value);
                var before = previous.get(metric);
                if (before != null) {
                    change.put(metric, value.subtract(before));
                    if (before.signum() != 0) {
                        percent.put(metric, value.subtract(before).multiply(BigDecimal.valueOf(100))
                                .divide(before.abs(), 1, RoundingMode.HALF_UP));
                    }
                }
            }
            result.add(new YearComparison(year, values, change, percent));
            previous = values;
        }
        return result;
    }

    private Map<AnnualMetric, BigDecimal> typedFigures(int year) {
        var values = new EnumMap<AnnualMetric, BigDecimal>(AnnualMetric.class);
        for (AnnualFigure figure : figures.findByYear(year)) {
            metricOf(figure.getMetric()).ifPresent(metric -> values.put(metric, figure.getAmount()));
        }
        return values;
    }

    private static Map<AnnualMetric, BigDecimal> calculatedFigures(YearOverview overview, Map<String, BigDecimal> previous) {
        var values = new EnumMap<AnnualMetric, BigDecimal>(AnnualMetric.class);
        if (overview.hasData()) {
            for (var metric : ANNUAL_ACTIVITY) {
                var computed = overview.yearly().get(metric.name());
                if (computed != null) {
                    values.put(metric, computed);
                }
            }
        }
        values.put(AnnualMetric.NET_VALUE, overview.yearly().getOrDefault("TOTAL_LIQUID", previousValue(previous, "NET_VALUE")));
        var investedThisYear = overview.yearly().getOrDefault("ANNUAL_INVESTED", BigDecimal.ZERO);
        var soldCostThisYear = overview.yearly().getOrDefault("ANNUAL_SOLD_COST", BigDecimal.ZERO);
        values.put(AnnualMetric.INVESTED,
                previousValue(previous, "INVESTED").add(investedThisYear).subtract(soldCostThisYear));
        return values;
    }

    private static BigDecimal previousValue(Map<String, BigDecimal> previous, String name) {
        return previous.getOrDefault(name, BigDecimal.ZERO);
    }

    private static Map<String, BigDecimal> byName(Map<AnnualMetric, BigDecimal> values) {
        var named = new HashMap<String, BigDecimal>();
        values.forEach((metric, value) -> named.put(metric.name(), value));
        return named;
    }

    private static Optional<AnnualMetric> metricOf(String name) {
        try {
            return Optional.of(AnnualMetric.valueOf(name));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
