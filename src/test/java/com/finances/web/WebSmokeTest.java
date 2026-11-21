package com.finances.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.finances.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

class WebSmokeTest extends IntegrationTestSupport {

    @Autowired MockMvc mvc;

    @BeforeEach
    void seed() {
        loadSampleLedger();
    }

    @ParameterizedTest
    @ValueSource(strings = {"/summary", "/revenues", "/earnings", "/expenses", "/investments", "/backup"})
    void pagesRender(String path) throws Exception {
        mvc.perform(get(path).param("year", "2026")).andExpect(status().isOk());
    }

    @Test
    void pastingAMonthAddsItsExpenses() throws Exception {
        mvc.perform(post("/expenses/paste").param("year", "2026").param("month", "3").param("text", "Coffee\t2,50"))
                .andExpect(status().is3xxRedirection());
        mvc.perform(get("/expenses").param("year", "2026").param("quarter", "1"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Coffee")));
    }

    @Test
    void previewingAPasteDoesNotSaveIt() throws Exception {
        mvc.perform(post("/expenses/paste/preview").param("year", "2026").param("month", "3").param("text", "Coffee\t2,50"))
                .andExpect(status().is3xxRedirection());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM expense WHERE description = 'Coffee'", Integer.class)).isZero();
    }

    @Test
    void rootRedirectsToSummary() throws Exception {
        mvc.perform(get("/")).andExpect(status().is3xxRedirection());
    }

    @Test
    void summaryShowsComputedMonthBalance() throws Exception {
        mvc.perform(get("/summary").param("year", "2026")).andExpect(content().string(org.hamcrest.Matchers.containsString("495,00")));
    }

    @Test
    void editingARevenueCellRequestsAPageRefresh() throws Exception {
        mvc.perform(put("/revenues/2026/2/GROSS_SALARY").param("amount", "1234.50"))
                .andExpect(status().isOk()).andExpect(header().string("HX-Refresh", "true"));
    }

    @Test
    void unknownRevenueFieldIsNotFound() throws Exception {
        mvc.perform(put("/revenues/2026/2/NOPE").param("amount", "1")).andExpect(status().isNotFound());
    }

    @Test
    void addingAndRemovingBankAccountsWorks() throws Exception {
        mvc.perform(post("/summary/accounts").param("name", "Savings").param("balance", "10").param("year", "2026"))
                .andExpect(status().isOk());
        var id = jdbc.queryForObject("SELECT id FROM bank_account WHERE name = 'Savings'", Long.class);
        mvc.perform(delete("/summary/accounts/" + id)).andExpect(status().isOk());
    }

    @Test
    void earningLinesCanBeAddedAndRemoved() throws Exception {
        mvc.perform(post("/earnings").param("year", "2026").param("month", "5")
                        .param("description", "Net Complement").param("amount", "17.5"))
                .andExpect(status().isOk());
        var id = jdbc.queryForObject("SELECT id FROM revenue_line WHERE description = 'Net Complement'", Long.class);
        mvc.perform(delete("/earnings/" + id)).andExpect(status().isOk());
    }

    @Test
    void comparisonEndpointReturnsYears() throws Exception {
        mvc.perform(get("/api/summary/comparison")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].year").value(2023));
    }

    @Test
    void investmentAssetCanBeAddedAndOpeningValueSet() throws Exception {
        mvc.perform(post("/investments/assets").param("name", "MSCI Japan").param("tags", "CHART_PORTFOLIO"))
                .andExpect(status().isOk());
        var id = jdbc.queryForObject("SELECT id FROM investment_asset WHERE name = 'MSCI Japan'", Long.class);
        mvc.perform(put("/investments/openings/2026/" + id).param("amount", "42")).andExpect(status().isOk());
        mvc.perform(put("/investments/contributions/2026/2/" + id).param("amount", "8")).andExpect(status().isOk());
    }

    @Test
    void salesAndDeletedInvestmentsCanBeManagedFromTheInvestmentsPage() throws Exception {
        mvc.perform(post("/investments/assets").param("name", "Crypto").param("tags", ""))
                .andExpect(status().isOk());
        var id = jdbc.queryForObject("SELECT id FROM investment_asset WHERE name = 'Crypto'", Long.class);
        mvc.perform(post("/investments/sales").param("year", "2026").param("month", "6").param("assetId", String.valueOf(id))
                        .param("proceeds", "120").param("cost", "100"))
                .andExpect(status().isOk());
        mvc.perform(delete("/investments/assets/" + id).param("fromYear", "2027")).andExpect(status().isOk());
        mvc.perform(get("/investments").param("year", "2027")).andExpect(status().isOk());
        mvc.perform(put("/investments/contributions/2027/1/" + id).param("amount", "5")).andExpect(status().isBadRequest());
        mvc.perform(put("/investments/assets/" + id + "/reopen")).andExpect(status().isOk());
        mvc.perform(post("/investments/sales").param("year", "2026").param("month", "13").param("assetId", String.valueOf(id))
                        .param("proceeds", "1").param("cost", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidMonthIsRejected() throws Exception {
        mvc.perform(put("/revenues/2026/13/GROSS_SALARY").param("amount", "1")).andExpect(status().isBadRequest());
    }

    @Test
    void invalidExpenseFormIsRejectedWithFieldMessage() throws Exception {
        mvc.perform(post("/expenses").param("year", "2026").param("month", "1").param("description", " ").param("amount", "5"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/expenses").param("year", "2026").param("month", "1").param("description", "x").param("amount", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void validExpenseCanBeAddedToggledAndDeleted() throws Exception {
        mvc.perform(post("/expenses").param("year", "2026").param("month", "3").param("description", "Coffee").param("amount", "2.5"))
                .andExpect(status().isOk());
        var id = jdbc.queryForObject("SELECT id FROM expense WHERE description = 'Coffee'", Long.class);

        mvc.perform(post("/expenses/" + id + "/toggle")).andExpect(status().isOk());
        mvc.perform(delete("/expenses/" + id)).andExpect(status().isOk());
    }

    @Test
    void onlyTheBaseYearFiguresCanBeTypedIn() throws Exception {
        mvc.perform(put("/summary/figures/2023/NET_VALUE").param("amount", "5")).andExpect(status().isOk());
        mvc.perform(put("/summary/figures/2024/NET_VALUE").param("amount", "5")).andExpect(status().isBadRequest());
    }

    @Test
    void quarterOutsideTheYearIsRejected() throws Exception {
        mvc.perform(get("/expenses").param("year", "2026").param("quarter", "5")).andExpect(status().isBadRequest());
        mvc.perform(get("/earnings").param("year", "2026").param("quarter", "4")).andExpect(status().isOk());
    }

    @Test
    void chartEndpointsReturnJson() throws Exception {
        mvc.perform(get("/api/investments/2026/charts/portfolio")).andExpect(status().isOk())
                .andExpect(jsonPath("$.labels[0]").value("S&P 500"));
        mvc.perform(get("/api/summary/2026/monthly")).andExpect(status().isOk())
                .andExpect(jsonPath("$.labels.length()").value(12));
        mvc.perform(get("/api/investments/2026/charts/unknown")).andExpect(status().isNotFound());
    }

    @Test
    void snapshotDownloadReturnsAttachment() throws Exception {
        mvc.perform(get("/backup/download")).andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")));
    }
}
