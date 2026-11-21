package com.finances.domain;

import com.finances.common.JsonMapConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "investment_asset")
public class InvestmentAsset {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true) private String name;
    @Column(nullable = false) private String tags = "";
    @Column(name = "sort_order", nullable = false) private int sortOrder;
    @Convert(converter = JsonMapConverter.class) private Map<String, Object> attributes = new LinkedHashMap<>();
    @Column(name = "closed_from_year") private Integer closedFromYear;

    public Set<String> tagSet() {
        if (tags == null || tags.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(tags.split(",")).map(String::trim).filter(t -> !t.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Long getId() {
        return id;
    }

    public InvestmentAsset setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public InvestmentAsset setName(String name) {
        this.name = name;
        return this;
    }

    public String getTags() {
        return tags;
    }

    public InvestmentAsset setTags(String tags) {
        this.tags = tags;
        return this;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public InvestmentAsset setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
        return this;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public InvestmentAsset setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
        return this;
    }

    /** The first year the investment no longer takes contributions, or null while it is still open. */
    public Integer getClosedFromYear() {
        return closedFromYear;
    }

    public InvestmentAsset setClosedFromYear(Integer closedFromYear) {
        this.closedFromYear = closedFromYear;
        return this;
    }

    public boolean isOpenIn(int year) {
        return closedFromYear == null || year < closedFromYear;
    }
}
