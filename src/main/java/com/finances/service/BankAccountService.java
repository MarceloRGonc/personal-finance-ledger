package com.finances.service;

import com.finances.calc.BankBalanceResolver;
import com.finances.common.Money;
import com.finances.common.Periods;
import com.finances.domain.BankAccount;
import com.finances.domain.BankBalance;
import com.finances.repository.BankAccountRepository;
import com.finances.repository.BankBalanceRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BankAccountService {

    private final BankAccountRepository accounts;
    private final BankBalanceRepository balances;

    public BankAccountService(BankAccountRepository accounts, BankBalanceRepository balances) {
        this.accounts = accounts;
        this.balances = balances;
    }

    @Transactional(readOnly = true)
    public List<AccountView> list(int year) {
        Periods.requireYear(year);
        var resolved = BankBalanceResolver.resolve(balances.findAll(), year);
        return accounts.findAllByOrderBySortOrderAscIdAsc().stream()
                .map(account -> new AccountView(account.getId(), account.getName(),
                        resolved.getOrDefault(account.getId(), BigDecimal.ZERO)))
                .toList();
    }

    public BankAccount add(String name, BigDecimal balance, int year) {
        Periods.requireYear(year);
        var trimmed = name.trim();
        if (accounts.findByNameIgnoreCase(trimmed).isPresent()) {
            throw new IllegalArgumentException("An account named " + trimmed + " already exists");
        }
        var nextOrder = accounts.findAllByOrderBySortOrderAscIdAsc().stream()
                .mapToInt(BankAccount::getSortOrder).max().orElse(0) + 1;
        var account = accounts.save(new BankAccount().setName(trimmed).setSortOrder(nextOrder));
        balances.save(new BankBalance().setAccountId(account.getId()).setYear(year)
                .setAmount(Money.requireReasonable(balance == null ? BigDecimal.ZERO : balance)));
        return account;
    }

    public void delete(long id) {
        var account = find(id);
        balances.deleteByAccountId(id);
        accounts.delete(account);
    }

    /** Sets the balance for one year only. Earlier years keep their balances. */
    public void updateBalance(long id, int year, BigDecimal amount) {
        Periods.requireYear(year);
        var value = Money.requireReasonable(amount);
        find(id);
        var entry = balances.findByAccountIdAndYear(id, year)
                .orElseGet(() -> new BankBalance().setAccountId(id).setYear(year));
        balances.save(entry.setAmount(value));
    }

    private BankAccount find(long id) {
        return accounts.findById(id).orElseThrow(() -> new NoSuchElementException("Account " + id + " not found"));
    }
}
