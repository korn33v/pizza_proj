package com.example.pizza.application.port.in.result;

import com.example.pizza.domain.model.Address;
import com.example.pizza.domain.model.Money;
import com.example.pizza.domain.model.Order;
import com.example.pizza.domain.model.PizzaItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

// [SEMINAR-05] DTO vs domain: это "результат use case" (application boundary), НЕ HTTP DTO и НЕ JPA Entity.
public record OrderResult(
    UUID id,
    AddressResult address,
    List<ItemResult> items,
    String status,
    MoneyResult totalPrice,
    String deliveryZone
) {

  public static OrderResult fromDomain(Order order) {
    return new OrderResult(
        order.id(),
        AddressResult.fromDomain(order.address()),
        order.items().stream().map(ItemResult::fromDomain).toList(),
        order.status().name(),
        MoneyResult.fromDomain(order.totalPrice()),
        order.deliveryZone()
    );
  }

  public record AddressResult(
      String city,
      String street,
      String house,
      String apartment,
      String postcode
  ) {
    public static AddressResult fromDomain(Address a) {
      return new AddressResult(a.city(), a.street(), a.house(), a.apartment(), a.postcode());
    }
  }

  public record ItemResult(
      String name,
      String size,
      int qty
  ) {
    public static ItemResult fromDomain(PizzaItem i) {
      return new ItemResult(i.name(), i.size().name(), i.qty());
    }
  }

  public record MoneyResult(BigDecimal amount, String currency) {
    public static MoneyResult fromDomain(Money m) {
      return new MoneyResult(m.amount(), m.currency());
    }
  }
}
