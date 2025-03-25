package com.onion.backend.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;

@Converter
public class DeviceListConverter implements AttributeConverter<List<Device>, String> {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public String convertToDatabaseColumn(List<Device> devices) {
    try {
      return objectMapper.writeValueAsString(devices == null ? new ArrayList<>() : devices);
    } catch (Exception e) {
      throw new RuntimeException("Could not serialize devices list", e);
    }
  }

  @Override
  public List<Device> convertToEntityAttribute(String dbData) {
    try {
      if (dbData == null || dbData.isBlank()) {
        return new ArrayList<>();
      }
      return objectMapper.readValue(dbData, new TypeReference<>() {});
    } catch (Exception e) {
      throw new RuntimeException("Could not deserialize devices list", e);
    }
  }
}
