package com.example.pizza.application.service;

import com.example.pizza.application.port.in.GetOrderBoardUseCase;
import com.example.pizza.application.port.in.command.GetOrderBoardQuery;
import com.example.pizza.application.port.in.result.OrderBoardReadModel;
import com.example.pizza.application.port.out.OrderRepositoryPort;
import com.example.pizza.application.service.exception.NotFoundException;
import com.example.pizza.application.service.exception.ValidationException;

public final class GetOrderBoardInteractor implements GetOrderBoardUseCase {

  private final OrderRepositoryPort orderRepository;

  public GetOrderBoardInteractor(OrderRepositoryPort orderRepository) {
    this.orderRepository = orderRepository;
  }

  @Override
  public OrderBoardReadModel get(GetOrderBoardQuery query) {
    if (query == null || query.orderId() == null) {
      throw new ValidationException("orderId must be provided");
    }

    var order = orderRepository.findById(query.orderId())
        .orElseThrow(() -> new NotFoundException("Order not found: " + query.orderId()));

    return OrderBoardReadModel.fromDomain(order);
  }
}
