package com.finances.service;

import java.math.BigDecimal;

public record AccountView(long id, String name, BigDecimal balance) {
}
