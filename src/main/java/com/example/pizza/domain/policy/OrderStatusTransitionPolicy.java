package com.example.pizza.domain.policy;

import com.example.pizza.domain.model.OrderStatus;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;

// [SEMINAR-09] Инварианты процесса: допустимые переходы статусов — доменная политика.
public final class OrderStatusTransitionPolicy {

  private final EnumMap<OrderStatus, Set<OrderStatus>> allowed = new EnumMap<>(OrderStatus.class);

  public OrderStatusTransitionPolicy() {
    allowed.put(OrderStatus.CREATED, EnumSet.of(OrderStatus.PAID, OrderStatus.CANCELLED));
    allowed.put(OrderStatus.PAID, EnumSet.of(OrderStatus.COOKING, OrderStatus.CANCELLED));
    allowed.put(OrderStatus.COOKING, EnumSet.of(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.CANCELLED));
    allowed.put(OrderStatus.OUT_FOR_DELIVERY, EnumSet.of(OrderStatus.DELIVERED));
    allowed.put(OrderStatus.DELIVERED, EnumSet.noneOf(OrderStatus.class));
    allowed.put(OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class));
  }

  public void assertCanTransition(OrderStatus from, OrderStatus to) {
    Set<OrderStatus> targets = allowed.getOrDefault(from, EnumSet.noneOf(OrderStatus.class));
    if (!targets.contains(to)) {
      throw new IllegalStateException("Illegal status transition: " + from + " -> " + to);
    }
  }

  // FAQ:
  // Q: Почему это не в JPA Entity?
  // A: Entity — persistence-деталь. Правила процесса должны жить в domain.
  // Q: Почему исключение, а не boolean?
  // A: Так правило само формулирует "нельзя". Application решит, как превратить это в ошибку наружу (например 409).
  // Q: Почему карта, а не if/switch в use case?
  // A: Правило централизовано и расширяемо, меньше дублирования.
}
