package com.finances.backup;

import com.finances.config.FinanceProperties;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BackupService {

    private static final Logger log = LoggerFactory.getLogger(BackupService.class);
    private static final Pattern SAFE_NAME = Pattern.compile("[A-Za-z0-9._-]+\\.db");
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

    private final DataSource dataSource;
    private final Flyway flyway;
    private final SqliteFileValidator validator;
    private final Path directory;
    private final int retention;
    private LocalDateTime lastStamp = LocalDateTime.MIN;

    public BackupService(DataSource dataSource, Flyway flyway, SqliteFileValidator validator, FinanceProperties properties) {
        this.dataSource = dataSource;
        this.flyway = flyway;
        this.validator = validator;
        this.directory = properties.backup().dir().toAbsolutePath().normalize();
        this.retention = properties.backup().retention();
    }

    public synchronized Path createSnapshot(String prefix) {
        var target = directory.resolve(prefix + "-" + nextStamp().format(STAMP) + ".db");
        try {
            Files.createDirectories(directory);
            vacuumInto(target);
            return target;
        } catch (IOException | SQLException e) {
            throw new BackupException("Could not create the snapshot", e);
        }
    }

    private LocalDateTime nextStamp() {
        var now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        lastStamp = now.isAfter(lastStamp) ? now : lastStamp.plus(1, ChronoUnit.MILLIS);
        return lastStamp;
    }

    public byte[] exportSnapshot() {
        Path folder = null;
        try {
            folder = Files.createTempDirectory("finances-export");
            var target = folder.resolve("export.db");
            vacuumInto(target);
            return Files.readAllBytes(target);
        } catch (IOException | SQLException e) {
            throw new BackupException("Could not export the database", e);
        } finally {
            deleteQuietly(folder == null ? null : folder.resolve("export.db"));
            deleteQuietly(folder);
        }
    }

    public List<BackupInfo> list() {
        if (!Files.isDirectory(directory)) {
            return List.of();
        }
        try (Stream<Path> files = Files.list(directory)) {
            return files.filter(p -> SAFE_NAME.matcher(p.getFileName().toString()).matches())
                    .map(this::describe)
                    .sorted(Comparator.comparing(BackupInfo::createdAt).reversed())
                    .toList();
        } catch (IOException e) {
            throw new BackupException("Could not list backups", e);
        }
    }

    public Path resolve(String fileName) {
        if (fileName == null || !SAFE_NAME.matcher(fileName).matches()) {
            throw new IllegalArgumentException("Invalid backup file name");
        }
        var path = directory.resolve(fileName).normalize();
        if (!path.startsWith(directory) || !Files.isRegularFile(path)) {
            throw new NoSuchElementException("Backup not found");
        }
        return path;
    }

    public synchronized void restoreFromUpload(InputStream upload) {
        Path staged = null;
        try {
            Files.createDirectories(directory);
            staged = Files.createTempFile(directory, "upload-", ".tmp");
            Files.copy(upload, staged, StandardCopyOption.REPLACE_EXISTING);
            restore(staged);
        } catch (IOException e) {
            throw new BackupException("Could not read the uploaded file", e);
        } finally {
            deleteQuietly(staged);
        }
    }

    public synchronized void restoreFromBackup(String fileName) {
        restore(resolve(fileName));
    }

    public synchronized void pruneAutomaticBackups() {
        var automatic = list().stream().filter(b -> b.fileName().startsWith("auto-")).toList();
        automatic.stream().skip(retention).forEach(b -> deleteQuietly(directory.resolve(b.fileName())));
    }

    private void restore(Path source) {
        validator.validate(source);
        createSnapshot("pre-restore");
        try (var connection = dataSource.getConnection(); var statement = connection.createStatement()) {
            statement.executeUpdate("restore from '" + quote(source) + "'");
        } catch (SQLException e) {
            throw new BackupException("Restore failed; the previous state was kept in a pre-restore snapshot", e);
        }
        flyway.migrate();
        log.info("Database restored from {}", source.getFileName());
    }

    private void vacuumInto(Path target) throws SQLException {
        try (var connection = dataSource.getConnection(); var statement = connection.createStatement()) {
            statement.execute("VACUUM INTO '" + quote(target) + "'");
        }
    }

    private static String quote(Path path) {
        return path.toAbsolutePath().toString().replace("'", "''");
    }

    private BackupInfo describe(Path path) {
        try {
            return new BackupInfo(path.getFileName().toString(), Files.size(path),
                    Files.getLastModifiedTime(path).toInstant());
        } catch (IOException e) {
            throw new BackupException("Could not read backup metadata", e);
        }
    }

    private static void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Could not delete {}", path, e);
        }
    }
}
