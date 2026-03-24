package com.example.pizza.application.service;

import com.example.pizza.application.port.in.result.OrderBoardReadModel;
import com.example.pizza.application.port.in.result.OrderResult;
import com.example.pizza.application.port.in.result.WorkflowStepResult;
import com.example.pizza.application.port.out.OrderRepositoryPort;
import com.example.pizza.application.service.exception.NotFoundException;
import com.example.pizza.application.service.exception.ValidationException;
import com.example.pizza.domain.model.Order;
import com.example.pizza.domain.policy.OrderStatusTransitionPolicy;

import com.example.pizza.application.port.in.StartCookingUseCase;
import com.example.pizza.application.port.in.command.StartCookingCommand;
import com.example.pizza.application.service.policy.CourierAssignmentPolicy;
import com.example.pizza.domain.event.CookingStartedEvent;

public final class StartCookingInteractor implements StartCookingUseCase {

  private final OrderRepositoryPort orderRepository;
  private final OrderStatusTransitionPolicy transitionPolicy;
  private final CourierAssignmentPolicy courierAssignmentPolicy;

  public StartCookingInteractor(
      OrderRepositoryPort orderRepository,
      OrderStatusTransitionPolicy transitionPolicy,
      CourierAssignmentPolicy courierAssignmentPolicy
  ) {
    this.orderRepository = orderRepository;
    this.transitionPolicy = transitionPolicy;
    this.courierAssignmentPolicy = courierAssignmentPolicy;
  }

  @Override
  public WorkflowStepResult start(StartCookingCommand command) {
    if (command == null || command.orderId() == null) {
      throw new ValidationException("orderId must be provided");
    }

    Order existing = orderRepository.findById(command.orderId())
        .orElseThrow(() -> new NotFoundException("Order not found: " + command.orderId()));

    Order updated = existing.startCooking(transitionPolicy);
    Order saved = orderRepository.save(updated);
    CookingStartedEvent event = new CookingStartedEvent(saved.id());

    return new WorkflowStepResult(
        saved.id(),
        "StartCooking",
        event.eventName(),
        OrderResult.fromDomain(saved),
        OrderBoardReadModel.fromDomain(saved),
        courierAssignmentPolicy.react(event)
    );
  }
}
