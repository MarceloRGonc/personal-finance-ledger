package com.finances.backup;

import java.time.Instant;

public record BackupInfo(String fileName, long sizeBytes, Instant createdAt) {
}
