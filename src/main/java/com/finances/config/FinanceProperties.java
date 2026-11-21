package com.finances.config;

import java.nio.file.Path;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "finance")
public record FinanceProperties(String dataDir, Backup backup) {

    public record Backup(Path dir, int retention, String cron) {
    }
}
