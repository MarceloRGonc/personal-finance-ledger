package com.finances.web;

import com.finances.common.Periods;
import com.finances.service.RevenueLineService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.stream.IntStream;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
class EarningController {

    private final RevenueLineService lines;

    EarningController(RevenueLineService lines) {
        this.lines = lines;
    }

    @GetMapping("/earnings")
    String page(@RequestParam(required = false) Integer year, @RequestParam(name = "quarter", required = false) Integer requestedQuarter, Model model) {
        var selected = Periods.yearOrCurrent(year);
        model.addAttribute("active", "earnings");
        model.addAttribute("year", selected);
        var quarter = QuarterSelection.quarter(selected, requestedQuarter);
        model.addAttribute("quarter", quarter);
        model.addAttribute("currentMonth", QuarterSelection.currentMonth(selected));
        model.addAttribute("quarters", IntStream.rangeClosed(1, RevenueLineService.QUARTERS).boxed().toList());
        model.addAttribute("grid", lines.grid(selected, quarter));
        return "earnings";
    }

    @PostMapping("/earnings")
    ResponseEntity<Void> add(@Valid EarningForm form) {
        lines.add(form.year(), form.month(), form.description(), form.amount());
        return Htmx.refresh();
    }

    @DeleteMapping("/earnings/{id}")
    ResponseEntity<Void> delete(@PathVariable long id) {
        lines.delete(id);
        return Htmx.refresh();
    }

    record EarningForm(@Min(1970) @Max(2200) int year, @Min(1) @Max(12) int month,
                       @NotBlank @Size(max = 120) String description,
                       @NotNull @Digits(integer = 10, fraction = 2) BigDecimal amount) {
    }
}
