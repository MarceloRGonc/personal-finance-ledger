package com.finances;

import com.finances.common.Money;
import com.finances.domain.AnnualFigure;
import com.finances.domain.BankAccount;
import com.finances.domain.BankBalance;
import com.finances.domain.Expense;
import com.finances.domain.InvestmentAsset;
import com.finances.domain.InvestmentBase;
import com.finances.domain.InvestmentContribution;
import com.finances.domain.LoanEntry;
import com.finances.domain.LoanEntryKind;
import com.finances.domain.RevenueLine;
import com.finances.domain.RevenueValue;
import com.finances.repository.AnnualFigureRepository;
import com.finances.repository.BankAccountRepository;
import com.finances.repository.BankBalanceRepository;
import com.finances.repository.ExpenseRepository;
import com.finances.repository.InvestmentAssetRepository;
import com.finances.repository.InvestmentBaseRepository;
import com.finances.repository.InvestmentContributionRepository;
import com.finances.repository.LoanEntryRepository;
import com.finances.repository.RevenueLineRepository;
import com.finances.repository.RevenueValueRepository;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
class SampleLedger {

    private static final int YEAR = 2026;
    private static final String PORTFOLIO_AND_ETFS = "CHART_PORTFOLIO,CHART_ETFS";
    private static final String PORTFOLIO_ONLY = "CHART_PORTFOLIO";

    private final RevenueValueRepository revenueValues;
    private final RevenueLineRepository revenueLines;
    private final ExpenseRepository expenses;
    private final InvestmentAssetRepository assets;
    private final InvestmentBaseRepository bases;
    private final InvestmentContributionRepository contributions;
    private final LoanEntryRepository loans;
    private final AnnualFigureRepository figures;
    private final BankAccountRepository accounts;
    private final BankBalanceRepository balances;

    SampleLedger(RevenueValueRepository revenueValues, RevenueLineRepository revenueLines, ExpenseRepository expenses,
                 InvestmentAssetRepository assets, InvestmentBaseRepository bases,
                 InvestmentContributionRepository contributions, LoanEntryRepository loans,
                 AnnualFigureRepository figures, BankAccountRepository accounts, BankBalanceRepository balances) {
        this.revenueValues = revenueValues;
        this.revenueLines = revenueLines;
        this.expenses = expenses;
        this.assets = assets;
        this.bases = bases;
        this.contributions = contributions;
        this.loans = loans;
        this.figures = figures;
        this.accounts = accounts;
        this.balances = balances;
    }

    void load() {
        january2026Revenue();
        earningLines();
        expenses();
        investments();
        loan();
        annualFigures();
        bankAccounts();
    }

    private void january2026Revenue() {
        revenueValue("GROSS_SALARY", "1000");
        revenueValue("GROSS_BONUS", "100");
        revenueValue("FOOD_ALLOWANCE", "10");
        revenueValue("IRS", "200");
        revenueValue("SS", "100");
    }

    private void earningLines() {
        earningLine(1, "Net Bonus", "50", 3);
        earningLine(1, "Interest", "115", 20);
        earningLine(2, "Refund", "21", 20);
    }

    private void expenses() {
        expense(1, "Rent", "300", 3, true);
        expense(1, "Outside the total", "999", 33, false);
        expense(2, "Gym", "40", 3, true);
    }

    private void investments() {
        var indexFund = asset("S&P 500", PORTFOLIO_AND_ETFS, 1);
        var pensionPlan = asset("Pension Plan", PORTFOLIO_ONLY, 2);
        var crypto = asset("Crypto", PORTFOLIO_AND_ETFS, 3);

        base(indexFund, "500");
        base(pensionPlan, "10000");
        base(crypto, "0");
        contribution(1, indexFund, "10");
        contribution(1, pensionPlan, "170");
        contribution(2, indexFund, "5");
    }

    private void loan() {
        loanEntry("Principal", "1000", LoanEntryKind.PRINCIPAL, 4);
        loanEntry("Opening " + YEAR, "800", LoanEntryKind.OPENING, 4);
        loanEntry("Payment 1", "100", LoanEntryKind.PAYMENT, 5);
        loanEntry("Payment 2", "100", LoanEntryKind.PAYMENT, 6);
    }

    private void annualFigures() {
        figure(2024, "NET_VALUE", "100");
        figure(2024, "INVESTED", "50");
        figure(2024, "GROSS_ANNUAL_INCOME", "1000");
        figure(2024, "NET_ANNUAL_INCOME", "800");
        figure(2024, "ANNUAL_INVESTED", "1234.56");
        figure(2025, "NET_VALUE", "200");
        figure(2025, "INVESTED", "60");
    }

    private void bankAccounts() {
        bankAccount("BankA", "100", 8);
        bankAccount("BankB", "50", 9);
    }

    private void revenueValue(String fieldCode, String amount) {
        revenueValues.save(new RevenueValue().setYear(YEAR).setMonth(1).setFieldCode(fieldCode).setAmount(money(amount)));
    }

    private void earningLine(int month, String description, String amount, int sortOrder) {
        revenueLines.save(new RevenueLine().setYear(YEAR).setMonth(month).setDescription(description)
                .setAmount(money(amount)).setSortOrder(sortOrder));
    }

    private void expense(int month, String description, String amount, int sortOrder, boolean counted) {
        expenses.save(new Expense().setYear(YEAR).setMonth(month).setDescription(description)
                .setAmount(money(amount)).setSortOrder(sortOrder).setCounted(counted));
    }

    private InvestmentAsset asset(String name, String tags, int sortOrder) {
        return assets.save(new InvestmentAsset().setName(name).setTags(tags).setSortOrder(sortOrder));
    }

    private void base(InvestmentAsset asset, String amount) {
        bases.save(new InvestmentBase().setYear(YEAR).setAssetId(asset.getId()).setAmount(money(amount)));
    }

    private void contribution(int month, InvestmentAsset asset, String amount) {
        contributions.save(new InvestmentContribution().setYear(YEAR).setMonth(month).setAssetId(asset.getId())
                .setAmount(money(amount)));
    }

    private void loanEntry(String label, String amount, LoanEntryKind kind, int sortOrder) {
        loans.save(new LoanEntry().setLoanName("Car loan").setLabel(label).setAmount(money(amount)).setKind(kind)
                .setSortOrder(sortOrder));
    }

    private void figure(int year, String metric, String amount) {
        figures.save(new AnnualFigure().setYear(year).setMetric(metric).setAmount(money(amount)));
    }

    private void bankAccount(String name, String balance, int sortOrder) {
        var account = accounts.save(new BankAccount().setName(name).setSortOrder(sortOrder));
        balances.save(new BankBalance().setAccountId(account.getId()).setYear(YEAR).setAmount(money(balance)));
    }

    private static BigDecimal money(String amount) {
        return Money.normalize(new BigDecimal(amount));
    }
}
