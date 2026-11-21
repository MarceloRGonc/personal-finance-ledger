package com.finances.config;

import com.finances.common.Periods;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.nio.file.Path;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * @param baseYear the year whose figures are typed in by hand; every later year is calculated from it
 */
@Validated
@ConfigurationProperties(prefix = "finance")
public record FinanceProperties(String dataDir, @Min(Periods.MIN_YEAR) @Max(Periods.MAX_YEAR) int baseYear,
                                Backup backup) {

    public record Backup(Path dir, int retention, String cron) {
    }
}
