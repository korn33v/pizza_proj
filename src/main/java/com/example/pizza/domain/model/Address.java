package com.example.pizza.domain.model;

import java.util.Objects;

public record Address(String city, String street, String house, String apartment, String postcode) {

  public Address {
    requireText(city, "city");
    requireText(street, "street");
    requireText(house, "house");
    requireText(postcode, "postcode");
  }

  private static void requireText(String value, String field) {
    Objects.requireNonNull(value, field);
    if (value.isBlank()) {
      throw new IllegalArgumentException(field + " must be non-blank");
    }
  }
}
