package com.example.pizza.application.service;

import com.example.pizza.application.port.in.result.OrderBoardReadModel;
import com.example.pizza.application.port.in.result.OrderResult;
import com.example.pizza.application.port.in.result.WorkflowStepResult;
import com.example.pizza.application.port.out.OrderRepositoryPort;
import com.example.pizza.application.service.exception.NotFoundException;
import com.example.pizza.application.service.exception.ValidationException;
import com.example.pizza.domain.model.Order;
import com.example.pizza.domain.policy.OrderStatusTransitionPolicy;

import com.example.pizza.application.port.in.AssignCourierUseCase;
import com.example.pizza.application.port.in.command.AssignCourierCommand;
import com.example.pizza.application.service.policy.DeliveryCompletionPolicy;
import com.example.pizza.domain.event.CourierAssignedEvent;

public final class AssignCourierInteractor implements AssignCourierUseCase {

  private final OrderRepositoryPort orderRepository;
  private final OrderStatusTransitionPolicy transitionPolicy;
  private final DeliveryCompletionPolicy deliveryCompletionPolicy;

  public AssignCourierInteractor(
      OrderRepositoryPort orderRepository,
      OrderStatusTransitionPolicy transitionPolicy,
      DeliveryCompletionPolicy deliveryCompletionPolicy
  ) {
    this.orderRepository = orderRepository;
    this.transitionPolicy = transitionPolicy;
    this.deliveryCompletionPolicy = deliveryCompletionPolicy;
  }

  @Override
  public WorkflowStepResult assign(AssignCourierCommand command) {
    if (command == null || command.orderId() == null) {
      throw new ValidationException("orderId must be provided");
    }

    Order existing = orderRepository.findById(command.orderId())
        .orElseThrow(() -> new NotFoundException("Order not found: " + command.orderId()));

    Order updated = existing.assignCourier(transitionPolicy);
    Order saved = orderRepository.save(updated);
    CourierAssignedEvent event = new CourierAssignedEvent(saved.id());

    return new WorkflowStepResult(
        saved.id(),
        "AssignCourier",
        event.eventName(),
        OrderResult.fromDomain(saved),
        OrderBoardReadModel.fromDomain(saved),
        deliveryCompletionPolicy.react(event)
    );
  }
}
