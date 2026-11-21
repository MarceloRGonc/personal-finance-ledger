package com.finances.formula;

import com.finances.domain.FormulaScope;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionException;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.stereotype.Service;

@Service
public class SpelFormulaService implements FormulaService {

    private static final Pattern CODE = Pattern.compile("[A-Z][A-Z0-9_]{0,63}");

    private final ExpressionParser parser = new SpelExpressionParser();
    private final Map<FormulaScope, List<CompiledRule>> rulesByScope;

    public SpelFormulaService(FormulaCatalog catalog) {
        this.rulesByScope = catalog.rules().stream()
                .map(this::compile)
                .collect(Collectors.groupingBy(rule -> rule.definition().scope(),
                        () -> new EnumMap<>(FormulaScope.class), Collectors.toList()));
    }

    @Override
    public Map<String, BigDecimal> evaluate(FormulaScope scope, Map<String, Object> variables) {
        var context = SimpleEvaluationContext.forReadOnlyDataBinding().build();
        variables.forEach(context::setVariable);
        var results = new LinkedHashMap<String, BigDecimal>();
        for (var rule : rulesByScope.getOrDefault(scope, List.of())) {
            var value = evaluateRule(rule, context);
            context.setVariable(rule.definition().code(), value);
            results.put(rule.definition().code(), value);
        }
        return results;
    }

    private CompiledRule compile(FormulaDefinition definition) {
        if (definition.code() == null || !CODE.matcher(definition.code()).matches()) {
            throw new FormulaException("Rule code must be upper-case letters, digits and underscores: " + definition.code());
        }
        try {
            return new CompiledRule(definition, parser.parseExpression(definition.expression()));
        } catch (ExpressionException e) {
            throw new FormulaException("Rule " + definition.code() + " has an invalid expression: " + e.getMessage(), e);
        }
    }

    private static BigDecimal evaluateRule(CompiledRule rule, EvaluationContext context) {
        var code = rule.definition().code();
        try {
            Object result = rule.expression().getValue(context);
            if (result instanceof BigDecimal decimal) {
                return decimal;
            }
            if (result instanceof Number number) {
                return new BigDecimal(number.toString());
            }
            if (result == null) {
                throw new FormulaException("Rule " + code + " produced no value");
            }
            throw new FormulaException("Rule " + code + " is not numeric");
        } catch (ExpressionException | ArithmeticException e) {
            throw new FormulaException("Rule " + code + " failed: " + e.getMessage(), e);
        }
    }

    private record CompiledRule(FormulaDefinition definition, Expression expression) {
    }
}
