package com.finances.common;

import java.math.BigDecimal;
import java.util.Optional;

public final class LenientNumbers {

    private LenientNumbers() {
    }

    public static Optional<BigDecimal> parse(String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        var text = raw.replaceAll("[\\s€$]", "");
        if (text.isEmpty()) {
            return Optional.empty();
        }
        var decimalAt = Math.max(text.lastIndexOf('.'), text.lastIndexOf(','));
        var normalized = decimalAt < 0
                ? text
                : text.substring(0, decimalAt).replaceAll("[.,]", "") + "." + text.substring(decimalAt + 1);
        try {
            return Optional.of(new BigDecimal(normalized));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
