package com.finances.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "annual_figure")
public class AnnualFigure {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private int year;
    @Column(nullable = false) private String metric;
    @Column(nullable = false) private BigDecimal amount;

    public Long getId() {
        return id;
    }

    public AnnualFigure setId(Long id) {
        this.id = id;
        return this;
    }

    public int getYear() {
        return year;
    }

    public AnnualFigure setYear(int year) {
        this.year = year;
        return this;
    }

    public String getMetric() {
        return metric;
    }

    public AnnualFigure setMetric(String metric) {
        this.metric = metric;
        return this;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public AnnualFigure setAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }
}
