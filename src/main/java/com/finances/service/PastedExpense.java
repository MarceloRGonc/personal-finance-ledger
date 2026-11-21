package com.finances.service;

import java.math.BigDecimal;

public record PastedExpense(String description, BigDecimal amount) {
}
