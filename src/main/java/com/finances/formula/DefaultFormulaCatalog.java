package com.finances.formula;

import com.finances.domain.FormulaScope;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * The default yearly, monthly and portfolio formulas. Edit this class to change how figures are calculated.
 * A MONTH rule becomes a column in the monthly roll-up, a YEAR rule becomes a summary figure, and a
 * PORTFOLIO rule feeds the investments page. A rule can use any earlier rule of its scope by its code.
 */
@Component
class DefaultFormulaCatalog implements FormulaCatalog {

    @Override
    public List<FormulaDefinition> rules() {
        return List.of(
                month("GROSS_REVENUE", "#rev['GROSS_SALARY'] + #rev['GROSS_BONUS']",
                        "Gross salary plus gross bonus"),
                month("NET_REVENUE", "#revEarnings + #earningLines - #revDeductions",
                        "EARNING revenue columns plus the monthly earnings lines, minus DEDUCTION columns"),
                month("INVESTMENTS", "#invTotal", "Sum of all investment contributions for the month"),
                month("EXPENSES", "#expenses", "Sum of counted expenses for the month"),
                month("SALE_PROCEEDS", "#saleProceeds", "Cash received from investments sold"),
                month("SOLD_COST", "#soldCost", "Cost of investments sold, removed from invested"),
                month("REALIZED_PROFIT", "#SALE_PROCEEDS - #SOLD_COST", "Profit on investments sold"),
                month("MONTH_BALANCE", "#NET_REVENUE - #INVESTMENTS + #SALE_PROCEEDS - #EXPENSES",
                        "Net revenue minus investments plus sale proceeds minus expenses"),

                year("GROSS_ANNUAL_INCOME", "#YEAR_GROSS_REVENUE", "Sum of monthly gross revenue"),
                year("NET_ANNUAL_INCOME", "#YEAR_NET_REVENUE", "Sum of monthly net revenue"),
                year("ANNUAL_INVESTED", "#YEAR_INVESTMENTS", "Sum of monthly investment contributions"),
                year("ANNUAL_EXPENSES", "#YEAR_EXPENSES", "Sum of monthly counted expenses"),
                year("ANNUAL_SALE_PROCEEDS", "#YEAR_SALE_PROCEEDS", "Sum of monthly sale proceeds"),
                year("ANNUAL_SOLD_COST", "#YEAR_SOLD_COST", "Sum of monthly cost of investments sold"),
                year("TOTAL_LIQUID",
                        "#prev['NET_VALUE'] + #NET_ANNUAL_INCOME - #ANNUAL_INVESTED + #ANNUAL_SALE_PROCEEDS - #ANNUAL_EXPENSES",
                        "Previous year net value plus net income minus investments, plus sale proceeds, minus expenses"),
                year("CURRENT_BALANCE",
                        "#prev['NET_VALUE'] + #prev['INVESTED'] + #NET_ANNUAL_INCOME - #ANNUAL_EXPENSES",
                        "Previous year net value and invested plus net income minus expenses"),
                year("BANK_TOTAL", "#bankTotal", "Sum of all bank account balances"),
                year("BANK_DIFFERENCE", "#BANK_TOTAL - #TOTAL_LIQUID", "Bank balances minus Total Liquid"),

                portfolio("INVESTED_TOTAL", "#holdingsTotal + #loansRemaining",
                        "Investment assets plus remaining loan balance"),
                portfolio("ETF_SUBTOTAL", "#tags['CHART_ETFS']", "Total value of assets tagged CHART_ETFS"));
    }

    private static FormulaDefinition month(String code, String expression, String description) {
        return new FormulaDefinition(code, FormulaScope.MONTH, expression, description);
    }

    private static FormulaDefinition year(String code, String expression, String description) {
        return new FormulaDefinition(code, FormulaScope.YEAR, expression, description);
    }

    private static FormulaDefinition portfolio(String code, String expression, String description) {
        return new FormulaDefinition(code, FormulaScope.PORTFOLIO, expression, description);
    }
}
