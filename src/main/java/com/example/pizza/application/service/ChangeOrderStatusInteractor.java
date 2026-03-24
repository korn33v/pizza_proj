package com.example.pizza.application.service;

import com.example.pizza.application.port.in.ChangeOrderStatusUseCase;
import com.example.pizza.application.port.in.command.ChangeOrderStatusCommand;
import com.example.pizza.application.port.in.result.OrderResult;
import com.example.pizza.application.port.out.OrderRepositoryPort;
import com.example.pizza.application.service.exception.BusinessRuleViolationException;
import com.example.pizza.application.service.exception.NotFoundException;
import com.example.pizza.application.service.exception.ValidationException;
import com.example.pizza.domain.model.Order;
import com.example.pizza.domain.model.OrderStatus;
import com.example.pizza.domain.policy.OrderStatusTransitionPolicy;

import java.util.Locale;

public final class ChangeOrderStatusInteractor implements ChangeOrderStatusUseCase {

  private final OrderRepositoryPort orderRepository;
  private final OrderStatusTransitionPolicy transitionPolicy;

  public ChangeOrderStatusInteractor(OrderRepositoryPort orderRepository, OrderStatusTransitionPolicy transitionPolicy) {
    this.orderRepository = orderRepository;
    this.transitionPolicy = transitionPolicy;
  }

  @Override
  public OrderResult changeStatus(ChangeOrderStatusCommand command) {
    if (command == null) {
      throw new ValidationException("command must be provided");
    }
    if (command.newStatus() == null || command.newStatus().isBlank()) {
      throw new ValidationException("newStatus must be non-blank");
    }

    OrderStatus target;
    try {
      target = OrderStatus.valueOf(command.newStatus().trim().toUpperCase(Locale.ROOT));
    } catch (Exception ex) {
      throw new ValidationException("Unknown status: " + command.newStatus());
    }

    Order existing = orderRepository.findById(command.orderId())
        .orElseThrow(() -> new NotFoundException("Order not found: " + command.orderId()));

    try {
      transitionPolicy.assertCanTransition(existing.status(), target);
    } catch (IllegalStateException ex) {
      throw new BusinessRuleViolationException(ex.getMessage());
    }

    Order updated = existing.withStatus(target);
    Order saved = orderRepository.save(updated);
    return OrderResult.fromDomain(saved);
  }

  // FAQ:
  // Q: Почему transitionPolicy в domain, а не в application?
  // A: Это правило бизнеса. Application лишь применяет его в сценарии use case.
  // Q: Почему BusinessRuleViolationException в application?
  // A: Application формирует семантику ошибки; web решает HTTP-код.
  // Q: Почему сохраняем новый Order?
  // A: Иммутабельность уменьшает побочные эффекты и упрощает тестирование.
}
