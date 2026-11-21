package com.finances.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component("fmt")
public class Fmt {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault());

    private final DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.of("pt", "PT"));

    public String money(BigDecimal value) {
        return value == null ? "" : new DecimalFormat("#,##0.00", symbols).format(value);
    }

    public String label(String code) {
        var words = code.toLowerCase(Locale.ROOT).split("_");
        var label = new StringBuilder();
        for (var word : words) {
            if (word.isEmpty()) {
                continue;
            }
            label.append(label.isEmpty() ? "" : " ").append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return label.toString();
    }

    public String monthName(int month) {
        return Months.label(month);
    }

    public String dateTime(Instant instant) {
        return DATE_TIME.format(instant);
    }

    public String plain(BigDecimal value) {
        return value == null ? "" : value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
