package com.finances.domain;

import com.finances.common.JsonMapConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.LinkedHashMap;
import java.util.Map;

@Entity
@Table(name = "bank_account")
public class BankAccount {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true) private String name;
    @Column(name = "sort_order", nullable = false) private int sortOrder;
    @Convert(converter = JsonMapConverter.class) private Map<String, Object> attributes = new LinkedHashMap<>();

    public Long getId() {
        return id;
    }

    public BankAccount setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public BankAccount setName(String name) {
        this.name = name;
        return this;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public BankAccount setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
        return this;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public BankAccount setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
        return this;
    }
}
