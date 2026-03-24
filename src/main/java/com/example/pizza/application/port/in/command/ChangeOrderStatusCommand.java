package com.example.pizza.application.port.in.command;

import java.util.Objects;
import java.util.UUID;

public record ChangeOrderStatusCommand(UUID orderId, String newStatus) {
  public ChangeOrderStatusCommand {
    Objects.requireNonNull(orderId, "orderId");
    Objects.requireNonNull(newStatus, "newStatus");
  }
}
