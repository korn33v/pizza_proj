package com.example.pizza.domain.model;

import java.util.Objects;

public record PizzaItem(String name, PizzaSize size, int qty) {

  public PizzaItem {
    Objects.requireNonNull(size, "size");
    Objects.requireNonNull(name, "name");
    if (name.isBlank()) {
      throw new IllegalArgumentException("name must be non-blank");
    }
    if (qty <= 0) {
      throw new IllegalArgumentException("qty must be > 0");
    }
  }
}
