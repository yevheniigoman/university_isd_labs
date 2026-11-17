package com.runningtracker.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.YearMonth;

/**
 * Stores a {@link YearMonth} as its {@code YYYY-MM} text form. A Goal Month
 * carries no day or time (REQ-F-15), so a date column would store a meaningless
 * day; the text form is also ordered correctly by a plain string sort.
 */
@Converter
public class YearMonthConverter implements AttributeConverter<YearMonth, String> {

    @Override
    public String convertToDatabaseColumn(YearMonth attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public YearMonth convertToEntityAttribute(String dbData) {
        return dbData == null ? null : YearMonth.parse(dbData);
    }
}
