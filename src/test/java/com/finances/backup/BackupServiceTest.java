package com.finances.backup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.finances.IntegrationTestSupport;
import com.finances.repository.ExpenseRepository;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class BackupServiceTest extends IntegrationTestSupport {

    @Autowired BackupService backups;
    @Autowired ExpenseRepository expenses;

    @Test
    void restoringASnapshotBringsBackDeletedData() {
        loadSampleLedger();
        var before = expenses.count();
        var snapshot = backups.createSnapshot("manual");

        expenses.deleteAll();
        assertThat(expenses.count()).isZero();
        backups.restoreFromBackup(snapshot.getFileName().toString());

        assertThat(expenses.count()).isEqualTo(before);
        assertThat(backups.list()).extracting(BackupInfo::fileName).anyMatch(n -> n.startsWith("pre-restore-"));
    }

    @Test
    void uploadedFileThatIsNotSqliteIsRejectedAndDataIsUntouched() throws IOException {
        loadSampleLedger();
        var before = expenses.count();

        assertThatThrownBy(() -> backups.restoreFromUpload(new ByteArrayInputStream("not a database".getBytes(StandardCharsets.UTF_8))))
                .isInstanceOf(BackupException.class);

        assertThat(expenses.count()).isEqualTo(before);
    }

    @Test
    void exportedSnapshotIsAValidSqliteFile() throws IOException {
        var bytes = backups.exportSnapshot();

        assertThat(new String(bytes, 0, 15, StandardCharsets.US_ASCII)).isEqualTo("SQLite format 3");
        var restoreInput = new ByteArrayInputStream(bytes);
        backups.restoreFromUpload(restoreInput);
    }

    @Test
    void backupFileNamesCannotEscapeTheBackupDirectory() {
        assertThatThrownBy(() -> backups.resolve("../finances.db")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> backups.resolve("a/b.db")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void pruningKeepsOnlyTheConfiguredNumberOfAutomaticBackups() throws IOException {
        for (int i = 0; i < 16; i++) {
            backups.createSnapshot("auto");
        }

        backups.pruneAutomaticBackups();

        assertThat(backups.list().stream().filter(b -> b.fileName().startsWith("auto-"))).hasSize(14);
        assertThat(Files.exists(backups.resolve(backups.list().get(0).fileName()))).isTrue();
    }
}
