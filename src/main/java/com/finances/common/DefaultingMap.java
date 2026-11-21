package com.finances.common;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public final class DefaultingMap extends HashMap<String, BigDecimal> {

    public DefaultingMap() {
    }

    public DefaultingMap(Map<String, BigDecimal> source) {
        super(source);
    }

    @Override
    public BigDecimal get(Object key) {
        return getOrDefault(key, BigDecimal.ZERO);
    }
}
