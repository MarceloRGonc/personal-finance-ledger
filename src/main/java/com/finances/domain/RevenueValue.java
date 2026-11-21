package com.finances.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "revenue_value")
public class RevenueValue {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private int year;
    @Column(nullable = false) private int month;
    @Column(name = "field_code", nullable = false) private String fieldCode;
    @Column(nullable = false) private BigDecimal amount;

    public Long getId() {
        return id;
    }

    public RevenueValue setId(Long id) {
        this.id = id;
        return this;
    }

    public int getYear() {
        return year;
    }

    public RevenueValue setYear(int year) {
        this.year = year;
        return this;
    }

    public int getMonth() {
        return month;
    }

    public RevenueValue setMonth(int month) {
        this.month = month;
        return this;
    }

    public String getFieldCode() {
        return fieldCode;
    }

    public RevenueValue setFieldCode(String fieldCode) {
        this.fieldCode = fieldCode;
        return this;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public RevenueValue setAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }
}
