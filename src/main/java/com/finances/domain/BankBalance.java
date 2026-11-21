package com.finances.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "bank_balance")
public class BankBalance {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "account_id", nullable = false) private long accountId;
    @Column(nullable = false) private int year;
    @Column(nullable = false) private BigDecimal amount;

    public Long getId() {
        return id;
    }

    public long getAccountId() {
        return accountId;
    }

    public BankBalance setAccountId(long accountId) {
        this.accountId = accountId;
        return this;
    }

    public int getYear() {
        return year;
    }

    public BankBalance setYear(int year) {
        this.year = year;
        return this;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BankBalance setAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }
}
