package com.finances.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/** An investment sold in a month: the proceeds come back as cash, the cost leaves the invested amount. */
@Entity
@Table(name = "investment_sale")
public class InvestmentSale {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private int year;
    @Column(nullable = false) private int month;
    @Column(name = "asset_id", nullable = false) private long assetId;
    @Column(nullable = false) private BigDecimal proceeds;
    @Column(nullable = false) private BigDecimal cost;

    public Long getId() {
        return id;
    }

    public InvestmentSale setId(Long id) {
        this.id = id;
        return this;
    }

    public int getYear() {
        return year;
    }

    public InvestmentSale setYear(int year) {
        this.year = year;
        return this;
    }

    public int getMonth() {
        return month;
    }

    public InvestmentSale setMonth(int month) {
        this.month = month;
        return this;
    }

    public long getAssetId() {
        return assetId;
    }

    public InvestmentSale setAssetId(long assetId) {
        this.assetId = assetId;
        return this;
    }

    public BigDecimal getProceeds() {
        return proceeds;
    }

    public InvestmentSale setProceeds(BigDecimal proceeds) {
        this.proceeds = proceeds;
        return this;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public InvestmentSale setCost(BigDecimal cost) {
        this.cost = cost;
        return this;
    }
}
