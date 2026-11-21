package com.finances.formula;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.finances.common.DefaultingMap;
import com.finances.domain.FormulaScope;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SpelFormulaServiceTest {

    private static FormulaDefinition month(String code, String expression) {
        return new FormulaDefinition(code, FormulaScope.MONTH, expression, "");
    }

    private static SpelFormulaService serviceWith(FormulaDefinition... rules) {
        return new SpelFormulaService(() -> List.of(rules));
    }

    @Test
    void evaluatesArithmeticOverBigDecimalMaps() {
        var service = serviceWith(month("GROSS", "#rev['SALARY'] + #rev['BONUS']"));
        var rev = new DefaultingMap(Map.of("SALARY", new BigDecimal("5029.00"), "BONUS", new BigDecimal("50.00")));

        var result = service.evaluate(FormulaScope.MONTH, Map.of("rev", rev));

        assertThat(result.get("GROSS")).isEqualByComparingTo("5079.00");
    }

    @Test
    void missingMapKeysCountAsZero() {
        var service = serviceWith(month("GROSS", "#rev['SALARY'] + #rev['UNKNOWN']"));
        var rev = new DefaultingMap(Map.of("SALARY", new BigDecimal("10")));

        assertThat(service.evaluate(FormulaScope.MONTH, Map.of("rev", rev)).get("GROSS")).isEqualByComparingTo("10");
    }

    @Test
    void laterRulesCanReferenceEarlierRulesByCode() {
        var service = serviceWith(month("NET", "#gross - 20"), month("BALANCE", "#NET - 5"));

        var result = service.evaluate(FormulaScope.MONTH, Map.of("gross", new BigDecimal("100")));

        assertThat(result.get("BALANCE")).isEqualByComparingTo("75");
    }

    @Test
    void rulesAreEvaluatedOnlyInTheirOwnScope() {
        var service = serviceWith(month("GROSS", "1 + 1"));

        assertThat(service.evaluate(FormulaScope.YEAR, Map.of())).isEmpty();
    }

    @Test
    void rejectsTypeReferencesSoRulesCannotRunArbitraryCode() {
        var service = serviceWith(month("EVIL", "T(java.lang.Runtime).getRuntime()"));

        assertThatThrownBy(() -> service.evaluate(FormulaScope.MONTH, Map.of()))
                .isInstanceOf(FormulaException.class)
                .hasMessageContaining("EVIL");
    }

    @Test
    void nonNumericResultIsAnError() {
        var service = serviceWith(month("TEXT", "'abc'"));

        assertThatThrownBy(() -> service.evaluate(FormulaScope.MONTH, Map.of()))
                .isInstanceOf(FormulaException.class)
                .hasMessageContaining("not numeric");
    }

    @Test
    void wellFormedCatalogIsAccepted() {
        serviceWith(month("NET_REVENUE", "#a - #b"));
    }

    @Test
    void badRuleCodeFailsWhenTheServiceIsBuilt() {
        assertThatThrownBy(() -> serviceWith(month("bad code", "1 + 1")))
                .isInstanceOf(FormulaException.class);
    }

    @Test
    void invalidExpressionFailsWhenTheServiceIsBuilt() {
        assertThatThrownBy(() -> serviceWith(month("OK", "1 +* ")))
                .isInstanceOf(FormulaException.class);
    }
}
