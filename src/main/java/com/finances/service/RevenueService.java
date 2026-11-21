package com.finances.service;

import com.finances.common.DefaultingMap;
import com.finances.common.Money;
import com.finances.common.Months;
import com.finances.common.Periods;
import com.finances.domain.RevenueValue;
import com.finances.repository.RevenueFieldRepository;
import com.finances.repository.RevenueValueRepository;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RevenueService {

    private final RevenueFieldRepository fields;
    private final RevenueValueRepository values;

    public RevenueService(RevenueFieldRepository fields, RevenueValueRepository values) {
        this.fields = fields;
        this.values = values;
    }

    @Transactional(readOnly = true)
    public RevenueGrid grid(int year) {
        var byMonth = new HashMap<Integer, Map<String, BigDecimal>>();
        var totals = new DefaultingMap();
        for (var value : values.findByYear(year)) {
            byMonth.computeIfAbsent(value.getMonth(), m -> new HashMap<>()).put(value.getFieldCode(), value.getAmount());
            totals.merge(value.getFieldCode(), value.getAmount(), BigDecimal::add);
        }
        return new RevenueGrid(year, fields.findAllByOrderBySortOrderAscIdAsc(), byMonth, totals);
    }

    public void setValue(int year, int month, String fieldCode, BigDecimal amount) {
        Periods.requireYear(year);
        Months.requireValid(month);
        fields.findByCode(fieldCode).orElseThrow(() -> new NoSuchElementException("Unknown revenue field " + fieldCode));
        var existing = values.findByYearAndMonthAndFieldCode(year, month, fieldCode);
        if (amount == null) {
            existing.ifPresent(values::delete);
            return;
        }
        var entity = existing.orElseGet(() -> new RevenueValue().setYear(year).setMonth(month).setFieldCode(fieldCode));
        values.save(entity.setAmount(Money.requireReasonable(amount)));
    }
}
