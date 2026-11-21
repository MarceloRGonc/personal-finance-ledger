package com.finances.service;

import java.util.List;

public final class ExpensePasteException extends IllegalArgumentException {

    private final List<String> errors;

    public ExpensePasteException(List<String> errors) {
        super(String.join("; ", errors));
        this.errors = List.copyOf(errors);
    }

    public List<String> errors() {
        return errors;
    }
}
