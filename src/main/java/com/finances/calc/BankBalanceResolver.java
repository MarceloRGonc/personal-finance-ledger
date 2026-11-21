package com.finances.calc;

import com.finances.domain.BankBalance;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/** Resolves each account's balance for a year: its latest balance up to and including that year. */
public final class BankBalanceResolver {

    private BankBalanceResolver() {
    }

    public static Map<Long, BigDecimal> resolve(Collection<BankBalance> balances, int year) {
        var byAccount = new HashMap<Long, TreeMap<Integer, BigDecimal>>();
        for (var balance : balances) {
            if (balance.getYear() <= year) {
                byAccount.computeIfAbsent(balance.getAccountId(), id -> new TreeMap<>())
                        .put(balance.getYear(), balance.getAmount());
            }
        }
        var resolved = new HashMap<Long, BigDecimal>();
        byAccount.forEach((accountId, history) -> resolved.put(accountId, history.lastEntry().getValue()));
        return resolved;
    }
}
