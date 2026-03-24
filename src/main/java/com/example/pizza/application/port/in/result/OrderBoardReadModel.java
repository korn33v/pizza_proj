package com.example.pizza.application.port.in.result;

import com.example.pizza.domain.model.Order;
import com.example.pizza.domain.model.OrderStatus;

import java.math.BigDecimal;
import java.util.UUID;

// [SEMINAR-32] Read Model = модель только для чтения.
// Она не защищает инварианты, а даёт удобное представление процесса для UI/доски/демо.
public record OrderBoardReadModel(
    UUID id,
    String currentStatus,
    BigDecimal totalAmount,
    String currency,
    String deliveryZone,
    boolean paymentReceived,
    boolean cookingStarted,
    boolean courierAssigned,
    boolean delivered,
    String nextSuggestedCommand
) {
  public static OrderBoardReadModel fromDomain(Order order) {
    boolean paymentReceived = hasReached(order.status(), OrderStatus.PAID);
    boolean cookingStarted = hasReached(order.status(), OrderStatus.COOKING);
    boolean courierAssigned = hasReached(order.status(), OrderStatus.OUT_FOR_DELIVERY);
    boolean delivered = order.status() == OrderStatus.DELIVERED;

    return new OrderBoardReadModel(
        order.id(),
        order.status().name(),
        order.totalPrice().amount(),
        order.totalPrice().currency(),
        order.deliveryZone(),
        paymentReceived,
        cookingStarted,
        courierAssigned,
        delivered,
        switch (order.status()) {
          case CREATED -> "ReceivePayment";
          case PAID -> "StartCooking";
          case COOKING -> "AssignCourier";
          case OUT_FOR_DELIVERY -> "DeliverOrder";
          default -> "None";
        }
    );
  }

  private static boolean hasReached(OrderStatus current, OrderStatus checkpoint) {
    if (current == OrderStatus.CANCELLED) {
      return false;
    }
    return current.ordinal() >= checkpoint.ordinal();
  }
}
