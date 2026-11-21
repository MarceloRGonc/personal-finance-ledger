package com.finances.web;

import com.finances.calc.AnnualHistoryService;
import com.finances.calc.MonthRow;
import com.finances.calc.YearComparison;
import com.finances.calc.YearOverviewService;
import com.finances.common.Periods;
import com.finances.domain.AnnualMetric;
import com.finances.service.AnnualFigureService;
import com.finances.service.BankAccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
class SummaryController {

    private final YearOverviewService overviews;
    private final AnnualHistoryService history;
    private final BankAccountService accounts;
    private final AnnualFigureService figures;

    SummaryController(YearOverviewService overviews, AnnualHistoryService history, BankAccountService accounts,
                      AnnualFigureService figures) {
        this.overviews = overviews;
        this.history = history;
        this.accounts = accounts;
        this.figures = figures;
    }

    @GetMapping("/")
    String home() {
        return "redirect:/summary";
    }

    @GetMapping("/summary")
    String summary(@RequestParam(required = false) Integer year, Model model) {
        var selected = Periods.yearOrCurrent(year);
        var annualHistory = history.history(selected);
        model.addAttribute("active", "summary");
        model.addAttribute("year", selected);
        model.addAttribute("overview", annualHistory.overview());
        model.addAttribute("columns", annualHistory.overview().months().get(0).values().keySet());
        model.addAttribute("history", annualHistory);
        model.addAttribute("comparisons", history.comparisons(annualHistory));
        model.addAttribute("metrics", AnnualMetric.values());
        model.addAttribute("accounts", accounts.list(selected));
        return "summary";
    }

    @PutMapping("/summary/figures/{year}/{metric}")
    ResponseEntity<Void> setFigure(@PathVariable int year, @PathVariable AnnualMetric metric,
                                   @RequestParam(required = false) BigDecimal amount) {
        figures.set(year, metric, amount);
        return Htmx.refresh();
    }

    @PostMapping("/summary/accounts")
    ResponseEntity<Void> addAccount(@Valid AccountForm form) {
        accounts.add(form.name(), form.balance(), form.year());
        return Htmx.refresh();
    }

    @DeleteMapping("/summary/accounts/{id}")
    ResponseEntity<Void> deleteAccount(@PathVariable long id) {
        accounts.delete(id);
        return Htmx.refresh();
    }

    @PutMapping("/summary/accounts/{id}/balance")
    ResponseEntity<Void> setBalance(@PathVariable long id, @RequestParam int year, @RequestParam BigDecimal balance) {
        accounts.updateBalance(id, year, balance);
        return Htmx.refresh();
    }

    @ResponseBody
    @GetMapping("/api/summary/{year}/monthly")
    MonthlyChart monthly(@PathVariable int year) {
        var rows = overviews.overview(Periods.requireYear(year)).months();
        return new MonthlyChart(
                rows.stream().map(MonthRow::label).toList(),
                column(rows, "NET_REVENUE"), column(rows, "EXPENSES"), column(rows, "INVESTMENTS"));
    }

    @ResponseBody
    @GetMapping("/api/summary/comparison")
    List<YearComparison> comparison(@RequestParam(required = false) Integer year) {
        return history.comparisons(history.history(Periods.yearOrCurrent(year)));
    }

    private static List<BigDecimal> column(List<MonthRow> rows, String code) {
        return rows.stream().map(row -> row.values().getOrDefault(code, BigDecimal.ZERO)).toList();
    }

    record MonthlyChart(List<String> labels, List<BigDecimal> netRevenue, List<BigDecimal> expenses,
                        List<BigDecimal> investments) {
    }

    record AccountForm(@NotBlank @Size(max = 60) String name, @Digits(integer = 10, fraction = 2) BigDecimal balance,
                       int year) {
    }
}
