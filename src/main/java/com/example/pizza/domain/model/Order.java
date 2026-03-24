package com.example.pizza.domain.model;

import com.example.pizza.domain.policy.OrderStatusTransitionPolicy;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

// [SEMINAR-04] Иммутабельный домен + инварианты:
// - record без сеттеров => нельзя "дозаполнить" объект после создания
// - инварианты в конструкторе => Order всегда валиден после создания
public record Order(
    UUID id,
    List<PizzaItem> items,
    Address address,
    OrderStatus status,
    Money totalPrice,
    String deliveryZone
) {

  public Order {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(items, "items");
    Objects.requireNonNull(address, "address");
    Objects.requireNonNull(status, "status");
    Objects.requireNonNull(totalPrice, "totalPrice");
    Objects.requireNonNull(deliveryZone, "deliveryZone");

    items = List.copyOf(items);
    if (items.isEmpty()) {
      throw new IllegalArgumentException("items must be non-empty");
    }
    if (deliveryZone.isBlank()) {
      throw new IllegalArgumentException("deliveryZone must be non-blank");
    }
  }

  public Order withStatus(OrderStatus newStatus) {
    return new Order(this.id, this.items, this.address, newStatus, this.totalPrice, this.deliveryZone);
  }

  public Order markPaid(OrderStatusTransitionPolicy transitionPolicy) {
    transitionPolicy.assertCanTransition(this.status, OrderStatus.PAID);
    return withStatus(OrderStatus.PAID);
  }

  public Order startCooking(OrderStatusTransitionPolicy transitionPolicy) {
    transitionPolicy.assertCanTransition(this.status, OrderStatus.COOKING);
    return withStatus(OrderStatus.COOKING);
  }

  public Order assignCourier(OrderStatusTransitionPolicy transitionPolicy) {
    transitionPolicy.assertCanTransition(this.status, OrderStatus.OUT_FOR_DELIVERY);
    return withStatus(OrderStatus.OUT_FOR_DELIVERY);
  }

  public Order deliver(OrderStatusTransitionPolicy transitionPolicy) {
    transitionPolicy.assertCanTransition(this.status, OrderStatus.DELIVERED);
    return withStatus(OrderStatus.DELIVERED);
  }


  // FAQ:
  // Q: Почему items = List.copyOf(...)?
  // A: Чтобы никто не мог изменить список "снаружи" после создания — домен остаётся иммутабельным.
  // Q: Почему проверки тут, а не только в контроллере?
  // A: Контроллер/DTO может быть "грязным". Домен защищает инварианты независимо от источника данных.
  // Q: Почему deliveryZone строкой?
  // A: Для kata достаточно. В проде можно выделить Value Object / Enum, но принцип тот же.
}
