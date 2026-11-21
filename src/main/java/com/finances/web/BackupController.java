package com.finances.web;

import com.finances.backup.BackupService;
import com.finances.common.Periods;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
class BackupController {

    private final BackupService backups;

    BackupController(BackupService backups) {
        this.backups = backups;
    }

    @GetMapping("/backup")
    String page(@RequestParam(required = false) Integer year, Model model) {
        model.addAttribute("active", "backup");
        model.addAttribute("year", Periods.yearOrCurrent(year));
        model.addAttribute("backups", backups.list());
        return "backup";
    }

    @GetMapping("/backup/download")
    ResponseEntity<byte[]> download() {
        return attachment(backups.exportSnapshot(), "finances-" + LocalDate.now() + ".db");
    }

    @GetMapping("/backup/files/{fileName}")
    ResponseEntity<byte[]> downloadStored(@PathVariable String fileName) throws IOException {
        return attachment(Files.readAllBytes(backups.resolve(fileName)), fileName);
    }

    @PostMapping("/backup/snapshots")
    ResponseEntity<Void> snapshot() {
        backups.createSnapshot("manual");
        return Htmx.refresh();
    }

    @PostMapping("/backup/restore")
    ResponseEntity<Void> restoreUpload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Choose a database file to restore");
        }
        try (var in = file.getInputStream()) {
            backups.restoreFromUpload(in);
        }
        return Htmx.refresh();
    }

    @PostMapping("/backup/files/{fileName}/restore")
    ResponseEntity<Void> restoreStored(@PathVariable String fileName) {
        backups.restoreFromBackup(fileName);
        return Htmx.refresh();
    }

    private static ResponseEntity<byte[]> attachment(byte[] content, String fileName) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(fileName).build().toString())
                .body(content);
    }
}
