package com.finances.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "investment_base")
public class InvestmentBase {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private int year;
    @Column(name = "asset_id", nullable = false) private long assetId;
    @Column(nullable = false) private BigDecimal amount;

    public Long getId() {
        return id;
    }

    public InvestmentBase setId(Long id) {
        this.id = id;
        return this;
    }

    public int getYear() {
        return year;
    }

    public InvestmentBase setYear(int year) {
        this.year = year;
        return this;
    }

    public long getAssetId() {
        return assetId;
    }

    public InvestmentBase setAssetId(long assetId) {
        this.assetId = assetId;
        return this;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public InvestmentBase setAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }
}
