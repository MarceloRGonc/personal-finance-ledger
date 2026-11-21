package com.finances;

import com.finances.config.FinanceProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(FinanceProperties.class)
public class FinancesApplication {

    public static void main(String[] args) throws IOException {
        Files.createDirectories(Path.of(System.getenv().getOrDefault("FINANCE_DATA_DIR", "./data")));
        SpringApplication.run(FinancesApplication.class, args);
    }
}
