package com.example.pizza.application.service;

import com.example.pizza.application.port.in.GetOrderUseCase;
import com.example.pizza.application.port.in.command.GetOrderQuery;
import com.example.pizza.application.port.in.result.OrderResult;
import com.example.pizza.application.port.out.OrderRepositoryPort;
import com.example.pizza.application.service.exception.NotFoundException;
import com.example.pizza.domain.model.Order;

public final class GetOrderInteractor implements GetOrderUseCase {

  private final OrderRepositoryPort orderRepository;

  public GetOrderInteractor(OrderRepositoryPort orderRepository) {
    this.orderRepository = orderRepository;
  }

  @Override
  public OrderResult get(GetOrderQuery query) {
    Order order = orderRepository.findById(query.orderId())
        .orElseThrow(() -> new NotFoundException("Order not found: " + query.orderId()));
    return OrderResult.fromDomain(order);
  }

  // FAQ:
  // Q: Почему не возвращаем JPA Entity?
  // A: Application boundary не должен зависеть от persistence-деталей. См. [SEMINAR-05], [SEMINAR-06], [SEMINAR-14].
  // Q: Почему NotFoundException из application?
  // A: Web adapter превращает это в 404, не заставляя domain знать про HTTP.
}
