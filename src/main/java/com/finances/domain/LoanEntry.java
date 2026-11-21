package com.finances.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "loan_entry")
public class LoanEntry {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "loan_name", nullable = false) private String loanName;
    @Column(nullable = false) private String label;
    @Column(nullable = false) private BigDecimal amount;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private LoanEntryKind kind;
    @Column(nullable = false) private boolean counted = true;
    @Column(name = "sort_order", nullable = false) private int sortOrder;

    public Long getId() {
        return id;
    }

    public LoanEntry setId(Long id) {
        this.id = id;
        return this;
    }

    public String getLoanName() {
        return loanName;
    }

    public LoanEntry setLoanName(String loanName) {
        this.loanName = loanName;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public LoanEntry setLabel(String label) {
        this.label = label;
        return this;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LoanEntry setAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public LoanEntryKind getKind() {
        return kind;
    }

    public LoanEntry setKind(LoanEntryKind kind) {
        this.kind = kind;
        return this;
    }

    public boolean isCounted() {
        return counted;
    }

    public LoanEntry setCounted(boolean counted) {
        this.counted = counted;
        return this;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public LoanEntry setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
        return this;
    }
}
