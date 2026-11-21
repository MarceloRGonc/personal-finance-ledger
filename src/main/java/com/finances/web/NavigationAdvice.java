package com.finances.web;

import com.finances.config.FinanceProperties;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackages = "com.finances.web")
class NavigationAdvice {

    private final int baseYear;

    NavigationAdvice(FinanceProperties properties) {
        this.baseYear = properties.baseYear();
    }

    @ModelAttribute("baseYear")
    int baseYear() {
        return baseYear;
    }
}
