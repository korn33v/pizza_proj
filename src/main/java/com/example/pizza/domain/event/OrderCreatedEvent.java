package com.example.pizza.domain.event;

import java.util.UUID;

public record OrderCreatedEvent(UUID orderId) implements DomainEvent {
  @Override
  public String eventName() {
    return "OrderCreated";
  }
}
