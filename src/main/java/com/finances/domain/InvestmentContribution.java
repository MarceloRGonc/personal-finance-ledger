package com.finances.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "investment_contribution")
public class InvestmentContribution {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private int year;
    @Column(nullable = false) private int month;
    @Column(name = "asset_id", nullable = false) private long assetId;
    @Column(nullable = false) private BigDecimal amount;

    public Long getId() {
        return id;
    }

    public InvestmentContribution setId(Long id) {
        this.id = id;
        return this;
    }

    public int getYear() {
        return year;
    }

    public InvestmentContribution setYear(int year) {
        this.year = year;
        return this;
    }

    public int getMonth() {
        return month;
    }

    public InvestmentContribution setMonth(int month) {
        this.month = month;
        return this;
    }

    public long getAssetId() {
        return assetId;
    }

    public InvestmentContribution setAssetId(long assetId) {
        this.assetId = assetId;
        return this;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public InvestmentContribution setAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }
}
