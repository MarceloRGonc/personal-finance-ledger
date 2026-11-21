package com.finances.web;

import com.finances.common.Money;
import com.finances.common.Months;
import com.finances.common.Periods;
import com.finances.common.Quarters;
import com.finances.service.ExpensePasteException;
import com.finances.service.ExpenseService;
import com.finances.service.PastedExpense;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
class ExpenseController {

    private final ExpenseService expenses;

    ExpenseController(ExpenseService expenses) {
        this.expenses = expenses;
    }

    @GetMapping("/expenses")
    String page(@RequestParam(required = false) Integer year, @RequestParam(name = "quarter", required = false) Integer requestedQuarter, Model model) {
        var selected = Periods.yearOrCurrent(year);
        model.addAttribute("active", "expenses");
        model.addAttribute("year", selected);
        var quarter = QuarterSelection.quarter(selected, requestedQuarter);
        model.addAttribute("quarter", quarter);
        model.addAttribute("currentMonth", QuarterSelection.currentMonth(selected));
        model.addAttribute("quarters", IntStream.rangeClosed(1, ExpenseService.QUARTERS).boxed().toList());
        model.addAttribute("grid", expenses.grid(selected, quarter));
        return "expenses";
    }

    @PostMapping("/expenses")
    ResponseEntity<Void> add(@Valid ExpenseForm form) {
        expenses.add(form.year(), form.month(), form.description(), form.amount());
        return Htmx.refresh();
    }

    @PostMapping("/expenses/paste")
    String paste(@Valid PasteForm form, RedirectAttributes redirect) {
        try {
            var count = expenses.paste(form.year(), form.month(), form.text(), form.replacing());
            redirect.addFlashAttribute("message", count + (count == 1 ? " expense saved" : " expenses saved"));
        } catch (ExpensePasteException e) {
            redirect.addFlashAttribute("pasteErrors", e.errors());
            redirect.addFlashAttribute("pasteText", form.text());
        }
        return pageFor(form.year(), form.month());
    }

    @PostMapping("/expenses/paste/preview")
    String preview(@Valid PasteForm form, RedirectAttributes redirect) {
        try {
            var rows = expenses.preview(form.text());
            redirect.addFlashAttribute("pasteRows", rows);
            redirect.addFlashAttribute("pasteTotal", Money.sum(rows.stream().map(PastedExpense::amount).toList()));
        } catch (ExpensePasteException e) {
            redirect.addFlashAttribute("pasteErrors", e.errors());
        }
        redirect.addFlashAttribute("pasteText", form.text());
        redirect.addFlashAttribute("pasteReplace", form.replacing());
        redirect.addFlashAttribute("pasteMonth", form.month());
        redirect.addFlashAttribute("pasteMonthLabel", Months.label(form.month()));
        return pageFor(form.year(), form.month());
    }

    @DeleteMapping("/expenses/{id}")
    ResponseEntity<Void> delete(@PathVariable long id) {
        expenses.delete(id);
        return Htmx.refresh();
    }

    @PostMapping("/expenses/{id}/toggle")
    ResponseEntity<Void> toggle(@PathVariable long id) {
        expenses.toggleCounted(id);
        return Htmx.refresh();
    }

    private static String pageFor(int year, int month) {
        return "redirect:/expenses?year=" + year + "&quarter=" + Quarters.ofMonth(month);
    }

    record PasteForm(@Min(1970) @Max(2200) int year, @Min(1) @Max(12) int month,
                     @Size(max = 20000) String text, Boolean replace) {

        boolean replacing() {
            return Boolean.TRUE.equals(replace);
        }
    }

    record ExpenseForm(@Min(1970) @Max(2200) int year, @Min(1) @Max(12) int month,
                       @NotBlank @Size(max = 120) String description,
                       @NotNull @Digits(integer = 10, fraction = 2) BigDecimal amount) {
    }
}