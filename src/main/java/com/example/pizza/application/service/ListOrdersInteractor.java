package com.example.pizza.application.service;

import com.example.pizza.application.port.in.ListOrdersUseCase;
import com.example.pizza.application.port.in.result.OrderSummaryResult;
import com.example.pizza.application.port.out.OrderRepositoryPort;
import com.example.pizza.domain.model.Order;

import java.util.Comparator;
import java.util.List;

// [SEMINAR-22] Use case для списка: чистая Java, без Spring.
// Полезно для UI: таблица может обновляться без того, чтобы контроллер знал про persistence.
public final class ListOrdersInteractor implements ListOrdersUseCase {

  private final OrderRepositoryPort orderRepository;

  public ListOrdersInteractor(OrderRepositoryPort orderRepository) {
    this.orderRepository = orderRepository;
  }

  @Override
  public List<OrderSummaryResult> list() {
    return orderRepository.findAll().stream()
        .sorted(Comparator.comparing((Order o) -> o.id().toString()).reversed())
        .map(OrderSummaryResult::fromDomain)
        .toList();
  }

  // FAQ:
  // Q: Почему list() в application, а не controller->JPA?
  // A: Чтобы сохранить границы и тестируемость: web зависит от порта, а не от persistence деталей.
  // Q: Почему summary, а не полный Order?
  // A: Разные use cases — разные представления. Это снижает связность и трафик.
  // Q: Где фильтры/пагинация?
  // A: Для kata не нужно. В проде добавляется через query-объекты и расширение портов.
}
