package com.finances.formula;

import java.util.List;

/** Source of the formula rules. Rules are evaluated in the order they are listed. */
@FunctionalInterface
public interface FormulaCatalog {

    List<FormulaDefinition> rules();
}
