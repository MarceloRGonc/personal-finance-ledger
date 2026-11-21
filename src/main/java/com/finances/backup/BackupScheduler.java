package com.finances.backup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
class BackupScheduler {

    private static final Logger log = LoggerFactory.getLogger(BackupScheduler.class);

    private final BackupService backups;

    BackupScheduler(BackupService backups) {
        this.backups = backups;
    }

    @Scheduled(cron = "${finance.backup.cron}")
    void runAutomaticBackup() {
        try {
            var file = backups.createSnapshot("auto");
            backups.pruneAutomaticBackups();
            log.info("Automatic backup written to {}", file.getFileName());
        } catch (BackupException e) {
            log.error("Automatic backup failed", e);
        }
    }
}
