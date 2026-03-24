package com.example.pizza.domain.event;

import java.util.UUID;

public record PaymentReceivedEvent(UUID orderId) implements DomainEvent {
  @Override
  public String eventName() {
    return "PaymentReceived";
  }
}
