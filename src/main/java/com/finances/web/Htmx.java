package com.finances.web;

import org.springframework.http.ResponseEntity;

final class Htmx {

    private Htmx() {
    }

    static ResponseEntity<Void> refresh() {
        return ResponseEntity.ok().header("HX-Refresh", "true").build();
    }
}
