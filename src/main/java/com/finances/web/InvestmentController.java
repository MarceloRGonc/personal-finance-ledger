package com.finances.web;

import com.finances.calc.ChartData;
import com.finances.calc.LoanService;
import com.finances.calc.PortfolioService;
import com.finances.common.Months;
import com.finances.common.Periods;
import com.finances.common.Quarters;
import com.finances.service.InvestmentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Map;
import java.util.NoSuchElementException;
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
class InvestmentController {

    private static final Map<String, String> CHART_TAGS = Map.of(
            "portfolio", PortfolioService.PORTFOLIO_CHART,
            "etfs", PortfolioService.ETF_CHART);

    private final InvestmentService investments;
    private final PortfolioService portfolio;
    private final LoanService loans;

    InvestmentController(InvestmentService investments, PortfolioService portfolio, LoanService loans) {
        this.investments = investments;
        this.portfolio = portfolio;
        this.loans = loans;
    }

    @GetMapping("/investments")
    String page(@RequestParam(required = false) Integer year, @RequestParam(name = "quarter", required = false) Integer requestedQuarter,
                Model model) {
        var selected = Periods.yearOrCurrent(year);
        model.addAttribute("active", "investments");
        model.addAttribute("year", selected);
        var quarter = QuarterSelection.quarter(selected, requestedQuarter);
        model.addAttribute("quarter", quarter);
        model.addAttribute("currentMonth", QuarterSelection.currentMonth(selected));
        model.addAttribute("quarters", Quarters.ALL);
        model.addAttribute("quarterMonths", Quarters.months(quarter));
        model.addAttribute("months", Months.ALL);
        model.addAttribute("grid", investments.grid(selected));
        model.addAttribute("portfolio", portfolio.portfolio(selected));
        model.addAttribute("loans", loans.summaries());
        return "investments";
    }

    @PutMapping("/investments/contributions/{year}/{month}/{assetId}")
    ResponseEntity<Void> setContribution(@PathVariable int year, @PathVariable int month, @PathVariable long assetId,
                                         @RequestParam(required = false) BigDecimal amount) {
        investments.setContribution(year, month, assetId, amount);
        return Htmx.refresh();
    }

    @PutMapping("/investments/openings/{year}/{assetId}")
    ResponseEntity<Void> setOpening(@PathVariable int year, @PathVariable long assetId,
                                    @RequestParam(required = false) BigDecimal amount) {
        investments.setOpeningValue(year, assetId, amount);
        return Htmx.refresh();
    }

    @PostMapping("/investments/assets")
    ResponseEntity<Void> addAsset(@Valid AssetForm form) {
        investments.addAsset(form.name(), form.tags());
        return Htmx.refresh();
    }

    @PutMapping("/investments/assets/{assetId}/tags")
    ResponseEntity<Void> setTags(@PathVariable long assetId, @RequestParam(required = false) String tags) {
        investments.updateTags(assetId, tags);
        return Htmx.refresh();
    }

    @DeleteMapping("/investments/assets/{assetId}")
    ResponseEntity<Void> deleteAsset(@PathVariable long assetId, @RequestParam int fromYear) {
        investments.closeFrom(assetId, fromYear);
        return Htmx.refresh();
    }

    @DeleteMapping("/investments/assets/{assetId}/permanent")
    ResponseEntity<Void> removeAsset(@PathVariable long assetId) {
        investments.removeAsset(assetId);
        return Htmx.refresh();
    }

    @PostMapping("/investments/assets/{assetId}/up")
    ResponseEntity<Void> moveAssetUp(@PathVariable long assetId) {
        investments.moveUp(assetId);
        return Htmx.refresh();
    }

    @PostMapping("/investments/assets/{assetId}/down")
    ResponseEntity<Void> moveAssetDown(@PathVariable long assetId) {
        investments.moveDown(assetId);
        return Htmx.refresh();
    }

    @PutMapping("/investments/assets/{assetId}/reopen")
    ResponseEntity<Void> reopenAsset(@PathVariable long assetId) {
        investments.reopen(assetId);
        return Htmx.refresh();
    }

    @PostMapping("/investments/sales")
    ResponseEntity<Void> recordSale(@Valid SaleForm form) {
        investments.recordSale(form.year(), form.month(), form.assetId(), form.proceeds(), form.cost());
        return Htmx.refresh();
    }

    @DeleteMapping("/investments/sales/{saleId}")
    ResponseEntity<Void> deleteSale(@PathVariable long saleId) {
        investments.deleteSale(saleId);
        return Htmx.refresh();
    }

    @ResponseBody
    @GetMapping("/api/investments/{year}/charts/{chart}")
    ChartData chart(@PathVariable int year, @PathVariable String chart) {
        var tag = CHART_TAGS.get(chart);
        if (tag == null) {
            throw new NoSuchElementException("Unknown chart " + chart);
        }
        return portfolio.chart(Periods.requireYear(year), tag);
    }

    record AssetForm(@NotBlank @Size(max = 60) String name, @Size(max = 200) String tags) {
    }

    record SaleForm(int year, int month, long assetId, @NotNull BigDecimal proceeds, @NotNull BigDecimal cost) {
    }
}
