package com.finances.common;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.LinkedHashMap;
import java.util.Map;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Converter
public class JsonMapConverter implements AttributeConverter<Map<String, Object>, String> {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();
    private static final TypeReference<LinkedHashMap<String, Object>> TYPE = new TypeReference<>() {
    };

    @Override
    public String convertToDatabaseColumn(Map<String, Object> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(attribute);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Custom attributes are not serializable", e);
        }
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return new LinkedHashMap<>();
        }
        try {
            return MAPPER.readValue(dbData, TYPE);
        } catch (JacksonException e) {
            throw new IllegalStateException("Stored custom attributes are not valid JSON", e);
        }
    }
}
