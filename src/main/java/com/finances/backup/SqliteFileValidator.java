package com.finances.backup;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
class SqliteFileValidator {

    private static final byte[] MAGIC = "SQLite format 3\0".getBytes(StandardCharsets.US_ASCII);
    private static final List<String> REQUIRED_TABLES = List.of("flyway_schema_history", "expense", "revenue_value", "annual_figure");

    void validate(Path file) {
        requireSqliteHeader(file);
        try (var connection = DriverManager.getConnection("jdbc:sqlite:" + file.toAbsolutePath());
             var statement = connection.createStatement()) {
            try (var result = statement.executeQuery("PRAGMA integrity_check")) {
                if (!result.next() || !"ok".equalsIgnoreCase(result.getString(1))) {
                    throw new BackupException("The file failed the SQLite integrity check");
                }
            }
            for (var table : REQUIRED_TABLES) {
                try (var result = statement.executeQuery(
                        "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = '" + table + "'")) {
                    if (!result.next()) {
                        throw new BackupException("The file is not a Personal Finance Ledger database (missing table " + table + ")");
                    }
                }
            }
        } catch (SQLException e) {
            throw new BackupException("The file could not be opened as a SQLite database", e);
        }
    }

    private void requireSqliteHeader(Path file) {
        try (InputStream in = Files.newInputStream(file)) {
            var header = in.readNBytes(MAGIC.length);
            if (!Arrays.equals(header, MAGIC)) {
                throw new BackupException("The file is not a SQLite database");
            }
        } catch (IOException e) {
            throw new BackupException("The file could not be read", e);
        }
    }
}
