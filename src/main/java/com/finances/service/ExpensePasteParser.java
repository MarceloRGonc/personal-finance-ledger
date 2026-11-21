package com.finances.service;

import com.finances.common.LenientNumbers;
import com.finances.common.Money;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

final class ExpensePasteParser {

    static final int MAX_DESCRIPTION = 120;
    private static final Pattern HEADER_DESCRIPTION = Pattern.compile("(?i)description|expense|item|name");
    private static final Pattern HEADER_AMOUNT = Pattern.compile("(?i)amount|value");

    private ExpensePasteParser() {
    }

    static List<PastedExpense> parse(String text) {
        var rows = new ArrayList<PastedExpense>();
        var errors = new ArrayList<String>();
        var lines = text == null ? new String[0] : text.split("\\R");
        for (int index = 0; index < lines.length; index++) {
            var raw = lines[index];
            if (raw.isBlank()) {
                continue;
            }
            var number = index + 1;
            var pieces = split(raw);
            if (pieces == null) {
                errors.add("Line " + number + ": expected a description and an amount");
            } else if (!isHeader(pieces, rows, errors)) {
                toExpense(number, pieces[0], pieces[1], errors).ifPresent(rows::add);
            }
        }
        if (!errors.isEmpty()) {
            throw new ExpensePasteException(errors);
        }
        if (rows.isEmpty()) {
            throw new ExpensePasteException(List.of("Paste at least one expense"));
        }
        return rows;
    }

    private static boolean isHeader(String[] pieces, List<PastedExpense> rows, List<String> errors) {
        return rows.isEmpty() && errors.isEmpty()
                && HEADER_DESCRIPTION.matcher(pieces[0]).matches()
                && HEADER_AMOUNT.matcher(pieces[1]).matches();
    }

    private static String[] split(String raw) {
        var line = raw.stripTrailing();
        int tabOrSemicolon = Math.max(line.lastIndexOf('\t'), line.lastIndexOf(';'));
        if (tabOrSemicolon >= 0) {
            return pair(line.substring(0, tabOrSemicolon), line.substring(tabOrSemicolon + 1));
        }
        var trimmed = line.strip();
        int space = lastWhitespace(trimmed);
        return space < 0 ? null : pair(trimmed.substring(0, space), trimmed.substring(space + 1));
    }

    private static String[] pair(String description, String amount) {
        return new String[]{description.strip(), amount.strip()};
    }

    private static int lastWhitespace(String text) {
        for (int index = text.length() - 1; index >= 0; index--) {
            if (Character.isWhitespace(text.charAt(index))) {
                return index;
            }
        }
        return -1;
    }

    private static Optional<PastedExpense> toExpense(int number, String description, String amountText,
                                                     List<String> errors) {
        if (description.isEmpty()) {
            errors.add("Line " + number + ": description is missing");
            return Optional.empty();
        }
        if (description.length() > MAX_DESCRIPTION) {
            errors.add("Line " + number + ": description is longer than " + MAX_DESCRIPTION + " characters");
            return Optional.empty();
        }
        var amount = LenientNumbers.parse(amountText);
        if (amount.isEmpty()) {
            errors.add("Line " + number + ": amount '" + amountText + "' is not a number");
            return Optional.empty();
        }
        try {
            return Optional.of(new PastedExpense(description, Money.requireReasonable(amount.get())));
        } catch (IllegalArgumentException e) {
            errors.add("Line " + number + ": amount is out of range");
            return Optional.empty();
        }
    }
}
