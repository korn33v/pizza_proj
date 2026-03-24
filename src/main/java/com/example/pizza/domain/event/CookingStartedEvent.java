package com.example.pizza.domain.event;

import java.util.UUID;

public record CookingStartedEvent(UUID orderId) implements DomainEvent {
  @Override
  public String eventName() {
    return "CookingStarted";
  }
}
