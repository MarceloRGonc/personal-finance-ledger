package com.finances.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LenientNumbersTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "7.620.44|7620.44",
            "1,234.56|1234.56",
            "1234,5|1234.5",
            "42|42",
            "-12.5|-12.5",
            "€ 3.000,10|3000.10"
    })
    void parsesMixedSeparators(String raw, String expected) {
        assertThat(LenientNumbers.parse(raw)).hasValueSatisfying(v -> assertThat(v).isEqualByComparingTo(new BigDecimal(expected)));
    }

    @Test
    void rejectsGarbageAndBlank() {
        assertThat(LenientNumbers.parse("abc")).isEmpty();
        assertThat(LenientNumbers.parse("  ")).isEmpty();
        assertThat(LenientNumbers.parse(null)).isEmpty();
    }
}
