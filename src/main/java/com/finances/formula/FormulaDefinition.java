package com.finances.formula;

import com.finances.domain.FormulaScope;

public record FormulaDefinition(String code, FormulaScope scope, String expression, String description) {
}
