package com.example.pizza.application.service;

import com.example.pizza.application.port.in.RemoveOrderUseCase;
import com.example.pizza.application.port.in.command.CancelOrderCommand;
import com.example.pizza.application.port.out.OrderRepositoryPort;
import com.example.pizza.application.service.exception.BusinessRuleViolationException;
import com.example.pizza.application.service.exception.NotFoundException;
import com.example.pizza.application.service.exception.ValidationException;
import com.example.pizza.domain.model.Order;
import com.example.pizza.domain.model.OrderStatus;
import com.example.pizza.domain.policy.OrderStatusTransitionPolicy;

public final class RemoveOrderInteractor implements RemoveOrderUseCase {
  private final OrderRepositoryPort repository;
  private final OrderStatusTransitionPolicy transitionPolicy;

  public RemoveOrderInteractor(OrderRepositoryPort repository, OrderStatusTransitionPolicy transitionPolicy) {
    this.repository = repository;
    this.transitionPolicy = transitionPolicy;
  }

  @Override
  public void remove(CancelOrderCommand command) {
    if (command == null || command.orderId() == null) {
      throw new ValidationException("orderId must be provided");
    }
    if (command.reason() == null || command.reason().isBlank()
        || command.reason().strip().length() > 1000) {
      throw new ValidationException("Cancellation reason must contain 1 to 1000 characters");
    }
    Order order = repository.findById(command.orderId())
        .orElseThrow(() -> new NotFoundException("Order not found: " + command.orderId()));
    // Apply the same business rules as cancellation, including already cancelled orders.
    if (!(order.status() == OrderStatus.CANCELLED
        && command.reason().strip().equals(order.cancellationReason()))) {
      try {
        order.cancel(command.reason(), transitionPolicy);
      } catch (IllegalStateException ex) {
        throw new BusinessRuleViolationException(ex.getMessage());
      }
    }
    repository.remove(command.orderId());
  }
}
