package com.javarush.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.Year;

@Converter(autoApply = true)
public class YearAttributeConverter implements AttributeConverter<Year, Short> {
    @Override
    public Short convertToDatabaseColumn(Year attribute) {
        return (short) attribute.getValue();
    }

    @Override
    public Year convertToEntityAttribute(Short dbData) {
        return Year.of(dbData);
    }
}
