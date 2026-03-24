package com.example.pizza.application.port.in.result;

import com.example.pizza.domain.model.Order;

import java.math.BigDecimal;
import java.util.UUID;

// [SEMINAR-22] Read-model на границе application: для списка нам не нужен полный Order.
// Это НЕ HTTP DTO и НЕ JPA Entity — это "результат use case" для любых входных адаптеров.
public record OrderSummaryResult(
    UUID id,
    String status,
    BigDecimal totalAmount,
    String currency,
    String deliveryZone
) {
  public static OrderSummaryResult fromDomain(Order o) {
    return new OrderSummaryResult(
        o.id(),
        o.status().name(),
        o.totalPrice().amount(),
        o.totalPrice().currency(),
        o.deliveryZone()
    );
  }
}
