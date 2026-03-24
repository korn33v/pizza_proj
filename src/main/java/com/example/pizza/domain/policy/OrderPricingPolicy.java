package com.example.pizza.domain.policy;

import com.example.pizza.domain.model.Money;
import com.example.pizza.domain.model.PizzaItem;
import com.example.pizza.domain.model.PizzaSize;

import java.math.BigDecimal;
import java.util.List;

// [SEMINAR-08] Где живут бизнес-правила: pricing — доменная политика, а не "if-ы" в контроллере.
public final class OrderPricingPolicy {

  public Money calculateTotal(List<PizzaItem> items) {
    BigDecimal total = BigDecimal.ZERO;

    for (PizzaItem item : items) {
      BigDecimal unitPrice = unitPrice(item.size());
      BigDecimal line = unitPrice.multiply(BigDecimal.valueOf(item.qty()));
      total = total.add(line);
    }

    return Money.eur(total);
  }

  private BigDecimal unitPrice(PizzaSize size) {
    return switch (size) {
      case SMALL -> new BigDecimal("8.00");
      case MEDIUM -> new BigDecimal("10.00");
      case LARGE -> new BigDecimal("12.00");
    };
  }

  // FAQ:
  // Q: Почему цены не в БД?
  // A: Для kata — политика в коде. В реале вынесите в каталог/конфиг, но через порт.
  // Q: Почему policy в domain, а не application?
  // A: Это "правило бизнеса", не orchestration use case.
  // Q: Почему BigDecimal, а не double?
  // A: Деньги — только десятичная арифметика, без ошибок округления.
}
