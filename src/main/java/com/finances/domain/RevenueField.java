package com.finances.domain;

import com.finances.common.JsonMapConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.LinkedHashMap;
import java.util.Map;

@Entity
public class RevenueField {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true) private String code;
    @Column(nullable = false) private String label;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private RevenueFieldKind kind;
    @Column(name = "sort_order", nullable = false) private int sortOrder;
    @Convert(converter = JsonMapConverter.class) private Map<String, Object> attributes = new LinkedHashMap<>();

    public Long getId() {
        return id;
    }

    public RevenueField setId(Long id) {
        this.id = id;
        return this;
    }

    public String getCode() {
        return code;
    }

    public RevenueField setCode(String code) {
        this.code = code;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public RevenueField setLabel(String label) {
        this.label = label;
        return this;
    }

    public RevenueFieldKind getKind() {
        return kind;
    }

    public RevenueField setKind(RevenueFieldKind kind) {
        this.kind = kind;
        return this;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public RevenueField setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
        return this;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public RevenueField setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
        return this;
    }
}
