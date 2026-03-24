package com.example.pizza.application.port.in.command;

import java.util.List;
import java.util.Objects;

// Application command — простой DTO на границе use case (без аннотаций Spring).
public record CreateOrderCommand(AddressCommand address, List<PizzaItemCommand> items) {

  public CreateOrderCommand {
    Objects.requireNonNull(address, "address");
    Objects.requireNonNull(items, "items");
    items = List.copyOf(items);
  }

  public record AddressCommand(
      String city,
      String street,
      String house,
      String apartment,
      String postcode
  ) {}

  public record PizzaItemCommand(
      String name,
      String size,
      int qty
  ) {}
}
