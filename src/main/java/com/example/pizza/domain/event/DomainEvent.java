package com.example.pizza.domain.event;

import java.util.UUID;

// [SEMINAR-30] Domain Event = факт, который уже произошёл в предметной области.
// В Event Storming это оранжевый стикер. В коде — отдельный объект, который фиксирует результат
// выполнения команды над агрегатом.
public interface DomainEvent {
  UUID orderId();
  String eventName();
}
