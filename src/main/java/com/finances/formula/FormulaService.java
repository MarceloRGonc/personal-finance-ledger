package com.finances.formula;

import com.finances.domain.FormulaScope;
import java.math.BigDecimal;
import java.util.Map;

public interface FormulaService {

    Map<String, BigDecimal> evaluate(FormulaScope scope, Map<String, Object> variables);
}
