package com.finances.domain;

import com.finances.common.JsonMapConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Entity
@Table(name = "revenue_line")
public class RevenueLine {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private int year;
    @Column(nullable = false) private int month;
    @Column(nullable = false) private String description;
    @Column(nullable = false) private BigDecimal amount;
    @Column(name = "sort_order", nullable = false) private int sortOrder;
    @Convert(converter = JsonMapConverter.class) private Map<String, Object> attributes = new LinkedHashMap<>();

    public Long getId() {
        return id;
    }

    public RevenueLine setId(Long id) {
        this.id = id;
        return this;
    }

    public int getYear() {
        return year;
    }

    public RevenueLine setYear(int year) {
        this.year = year;
        return this;
    }

    public int getMonth() {
        return month;
    }

    public RevenueLine setMonth(int month) {
        this.month = month;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public RevenueLine setDescription(String description) {
        this.description = description;
        return this;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public RevenueLine setAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public RevenueLine setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
        return this;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public RevenueLine setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
        return this;
    }
}
