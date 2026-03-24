package com.example.pizza.application.port.in.command;

import java.util.Objects;
import java.util.UUID;

public record GetOrderQuery(UUID orderId) {
  public GetOrderQuery {
    Objects.requireNonNull(orderId, "orderId");
  }
}
