package com.finances.domain;

public enum AnnualMetric {
    NET_VALUE("Net Value"),
    INVESTED("Invested"),
    GROSS_ANNUAL_INCOME("Gross Annual Income"),
    NET_ANNUAL_INCOME("Net Annual Income"),
    ANNUAL_INVESTED("Annual Invested");

    private final String label;

    AnnualMetric(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
