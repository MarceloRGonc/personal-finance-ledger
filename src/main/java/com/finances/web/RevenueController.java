package com.finances.web;

import com.finances.common.Months;
import com.finances.common.Periods;
import com.finances.service.RevenueService;
import java.math.BigDecimal;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
class RevenueController {

    private final RevenueService revenues;

    RevenueController(RevenueService revenues) {
        this.revenues = revenues;
    }

    @GetMapping("/revenues")
    String page(@RequestParam(required = false) Integer year, Model model) {
        var selected = Periods.yearOrCurrent(year);
        model.addAttribute("active", "revenues");
        model.addAttribute("year", selected);
        model.addAttribute("grid", revenues.grid(selected));
        model.addAttribute("months", Months.ALL);
        return "revenues";
    }

    @PutMapping("/revenues/{year}/{month}/{fieldCode}")
    ResponseEntity<Void> setCell(@PathVariable int year, @PathVariable int month, @PathVariable String fieldCode,
                                 @RequestParam(required = false) BigDecimal amount) {
        revenues.setValue(year, month, fieldCode, amount);
        return Htmx.refresh();
    }

}
