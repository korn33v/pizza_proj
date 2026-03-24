package com.example.pizza.domain.event;

import java.util.UUID;

public record OrderDeliveredEvent(UUID orderId) implements DomainEvent {
  @Override
  public String eventName() {
    return "OrderDelivered";
  }
}
