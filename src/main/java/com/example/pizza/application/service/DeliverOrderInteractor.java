package com.example.pizza.application.service;

import com.example.pizza.application.port.in.result.OrderBoardReadModel;
import com.example.pizza.application.port.in.result.OrderResult;
import com.example.pizza.application.port.in.result.WorkflowStepResult;
import com.example.pizza.application.port.out.OrderRepositoryPort;
import com.example.pizza.application.service.exception.NotFoundException;
import com.example.pizza.application.service.exception.ValidationException;
import com.example.pizza.domain.model.Order;
import com.example.pizza.domain.policy.OrderStatusTransitionPolicy;

import com.example.pizza.application.port.in.DeliverOrderUseCase;
import com.example.pizza.application.port.in.command.DeliverOrderCommand;
import com.example.pizza.application.port.in.result.PolicyHintResult;
import com.example.pizza.domain.event.OrderDeliveredEvent;

public final class DeliverOrderInteractor implements DeliverOrderUseCase {

  private final OrderRepositoryPort orderRepository;
  private final OrderStatusTransitionPolicy transitionPolicy;

  public DeliverOrderInteractor(OrderRepositoryPort orderRepository, OrderStatusTransitionPolicy transitionPolicy) {
    this.orderRepository = orderRepository;
    this.transitionPolicy = transitionPolicy;
  }

  @Override
  public WorkflowStepResult deliver(DeliverOrderCommand command) {
    if (command == null || command.orderId() == null) {
      throw new ValidationException("orderId must be provided");
    }

    Order existing = orderRepository.findById(command.orderId())
        .orElseThrow(() -> new NotFoundException("Order not found: " + command.orderId()));

    Order updated = existing.deliver(transitionPolicy);
    Order saved = orderRepository.save(updated);
    OrderDeliveredEvent event = new OrderDeliveredEvent(saved.id());

    return new WorkflowStepResult(
        saved.id(),
        "DeliverOrder",
        event.eventName(),
        OrderResult.fromDomain(saved),
        OrderBoardReadModel.fromDomain(saved),
        new PolicyHintResult("WorkflowCompleted", "None", "Заказ завершён, дальнейших команд не требуется.")
    );
  }
}
