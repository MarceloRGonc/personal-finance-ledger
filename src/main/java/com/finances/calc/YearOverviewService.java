package com.finances.calc;

import com.finances.common.DefaultingMap;
import com.finances.common.Money;
import com.finances.common.Months;
import com.finances.domain.Expense;
import com.finances.domain.FormulaScope;
import com.finances.domain.InvestmentAsset;
import com.finances.domain.InvestmentContribution;
import com.finances.domain.InvestmentSale;
import com.finances.domain.RevenueField;
import com.finances.domain.RevenueFieldKind;
import com.finances.domain.RevenueLine;
import com.finances.domain.RevenueValue;
import com.finances.formula.FormulaService;
import com.finances.repository.BankBalanceRepository;
import com.finances.repository.ExpenseRepository;
import com.finances.repository.InvestmentAssetRepository;
import com.finances.repository.InvestmentContributionRepository;
import com.finances.repository.InvestmentSaleRepository;
import com.finances.repository.RevenueFieldRepository;
import com.finances.repository.RevenueLineRepository;
import com.finances.repository.RevenueValueRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class YearOverviewService {

    private final RevenueFieldRepository fields;
    private final RevenueValueRepository revenues;
    private final RevenueLineRepository revenueLines;
    private final InvestmentContributionRepository contributions;
    private final InvestmentAssetRepository assets;
    private final InvestmentSaleRepository sales;
    private final ExpenseRepository expenses;
    private final BankBalanceRepository balances;
    private final FormulaService formulas;

    public YearOverviewService(RevenueFieldRepository fields, RevenueValueRepository revenues,
                               RevenueLineRepository revenueLines, InvestmentContributionRepository contributions,
                               InvestmentAssetRepository assets, InvestmentSaleRepository sales,
                               ExpenseRepository expenses,
                               BankBalanceRepository balances, FormulaService formulas) {
        this.fields = fields;
        this.revenues = revenues;
        this.revenueLines = revenueLines;
        this.contributions = contributions;
        this.assets = assets;
        this.sales = sales;
        this.expenses = expenses;
        this.balances = balances;
        this.formulas = formulas;
    }

    public YearOverview overview(int year) {
        return overview(year, Map.of());
    }

    /** @param previousYear the previous year's figures by metric name, used for the carry-forward formulas */
    public YearOverview overview(int year, Map<String, BigDecimal> previousYear) {
        var kinds = fields.findAllByOrderBySortOrderAscIdAsc().stream()
                .collect(Collectors.toMap(RevenueField::getCode, RevenueField::getKind));
        var revenueByMonth = revenues.findByYear(year).stream().collect(Collectors.groupingBy(RevenueValue::getMonth));
        var linesByMonth = revenueLines.findByYearOrderByMonthAscSortOrderAscIdAsc(year).stream()
                .collect(Collectors.groupingBy(RevenueLine::getMonth));
        var contributionsByMonth = contributions.findByYear(year).stream()
                .collect(Collectors.groupingBy(InvestmentContribution::getMonth));
        var salesByMonth = sales.findByYear(year).stream()
                .collect(Collectors.groupingBy(InvestmentSale::getMonth));
        var assetNames = assets.findAll().stream().collect(Collectors.toMap(InvestmentAsset::getId, InvestmentAsset::getName));
        var expenseByMonth = expenses.findByYearOrderByMonthAscSortOrderAscIdAsc(year).stream()
                .filter(Expense::isCounted)
                .collect(Collectors.groupingBy(Expense::getMonth,
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)));

        var rows = new ArrayList<MonthRow>();
        for (int month : Months.ALL) {
            var variables = monthVariables(year, month, kinds,
                    revenueByMonth.getOrDefault(month, List.of()),
                    linesByMonth.getOrDefault(month, List.of()),
                    contributionsByMonth.getOrDefault(month, List.of()),
                    salesByMonth.getOrDefault(month, List.of()),
                    assetNames,
                    expenseByMonth.getOrDefault(month, BigDecimal.ZERO));
            rows.add(new MonthRow(month, Months.label(month), formulas.evaluate(FormulaScope.MONTH, variables)));
        }
        var totals = totals(rows);
        var yearly = formulas.evaluate(FormulaScope.YEAR, yearVariables(year, totals, previousYear));
        var hasData = !revenueByMonth.isEmpty() || !linesByMonth.isEmpty()
                || !contributionsByMonth.isEmpty() || !salesByMonth.isEmpty() || !expenseByMonth.isEmpty();
        return new YearOverview(year, hasData, rows, totals, yearly);
    }

    private Map<String, Object> monthVariables(int year, int month, Map<String, RevenueFieldKind> kinds,
                                               List<RevenueValue> revenueValues, List<RevenueLine> lines,
                                               List<InvestmentContribution> monthContributions,
                                               List<InvestmentSale> monthSales,
                                               Map<Long, String> assetNames, BigDecimal expenseTotal) {
        var rev = new DefaultingMap();
        var earnings = BigDecimal.ZERO;
        var deductions = BigDecimal.ZERO;
        for (var value : revenueValues) {
            rev.merge(value.getFieldCode(), value.getAmount(), BigDecimal::add);
            switch (kinds.getOrDefault(value.getFieldCode(), RevenueFieldKind.INFO)) {
                case EARNING -> earnings = earnings.add(value.getAmount());
                case DEDUCTION -> deductions = deductions.add(value.getAmount());
                case INFO -> { }
            }
        }
        var lineTotal = Money.sum(lines.stream().map(RevenueLine::getAmount).toList());

        var inv = new DefaultingMap();
        monthContributions.forEach(c -> inv.merge(assetNames.getOrDefault(c.getAssetId(), "#" + c.getAssetId()),
                c.getAmount(), BigDecimal::add));
        var invTotal = Money.sum(monthContributions.stream().map(InvestmentContribution::getAmount).toList());

        Map<String, Object> variables = new HashMap<>();
        variables.put("year", year);
        variables.put("month", month);
        variables.put("rev", rev);
        variables.put("revEarnings", earnings);
        variables.put("revDeductions", deductions);
        variables.put("earningLines", lineTotal);
        variables.put("inv", inv);
        variables.put("invTotal", invTotal);
        variables.put("saleProceeds", Money.sum(monthSales.stream().map(InvestmentSale::getProceeds).toList()));
        variables.put("soldCost", Money.sum(monthSales.stream().map(InvestmentSale::getCost).toList()));
        variables.put("expenses", expenseTotal);
        return variables;
    }

    private Map<String, Object> yearVariables(int year, Map<String, BigDecimal> totals, Map<String, BigDecimal> previousYear) {
        Map<String, Object> variables = new HashMap<>();
        totals.forEach((code, total) -> variables.put("YEAR_" + code, total));
        variables.put("prev", new DefaultingMap(previousYear));
        variables.put("year", year);
        variables.put("bankTotal", Money.sum(BankBalanceResolver.resolve(balances.findAll(), year).values()));
        return variables;
    }

    private static Map<String, BigDecimal> totals(List<MonthRow> rows) {
        var totals = new LinkedHashMap<String, BigDecimal>();
        for (var row : rows) {
            row.values().forEach((code, value) -> totals.merge(code, value, BigDecimal::add));
        }
        return totals;
    }
}
