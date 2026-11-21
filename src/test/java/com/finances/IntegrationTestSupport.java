package com.finances;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class IntegrationTestSupport {

    private static final Path DATA = createDataDirectory();

    private static final String[] DATA_TABLES = {"expense", "revenue_value", "revenue_line",
            "investment_contribution", "investment_sale", "investment_base", "investment_asset", "loan_entry",
            "annual_figure", "bank_balance", "bank_account"};

    @Autowired
    protected JdbcTemplate jdbc;
    @Autowired
    private SampleLedger sampleLedger;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("finance.data-dir", DATA::toString);
        registry.add("finance.backup.dir", () -> DATA.resolve("backups").toString());
    }

    @BeforeEach
    void cleanData() {
        for (var table : DATA_TABLES) {
            jdbc.execute("DELETE FROM " + table);
        }
        jdbc.execute("DELETE FROM revenue_field WHERE code NOT IN ('GROSS_SALARY','GROSS_BONUS','FOOD_ALLOWANCE','IRS','SS','OTHER_TAXES')");
    }

    protected void loadSampleLedger() {
        sampleLedger.load();
    }

    private static Path createDataDirectory() {
        try {
            return Files.createTempDirectory("finances-test");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
