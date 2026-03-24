package com.example.pizza.application.service;

import com.example.pizza.application.port.in.result.OrderBoardReadModel;
import com.example.pizza.application.port.in.result.OrderResult;
import com.example.pizza.application.port.in.result.WorkflowStepResult;
import com.example.pizza.application.port.out.OrderRepositoryPort;
import com.example.pizza.application.service.exception.NotFoundException;
import com.example.pizza.application.service.exception.ValidationException;
import com.example.pizza.domain.model.Order;
import com.example.pizza.domain.policy.OrderStatusTransitionPolicy;

import com.example.pizza.application.port.in.ReceivePaymentUseCase;
import com.example.pizza.application.port.in.command.ReceivePaymentCommand;
import com.example.pizza.application.service.policy.KitchenStartPolicy;
import com.example.pizza.domain.event.PaymentReceivedEvent;

public final class ReceivePaymentInteractor implements ReceivePaymentUseCase {

  private final OrderRepositoryPort orderRepository;
  private final OrderStatusTransitionPolicy transitionPolicy;
  private final KitchenStartPolicy kitchenStartPolicy;

  public ReceivePaymentInteractor(
      OrderRepositoryPort orderRepository,
      OrderStatusTransitionPolicy transitionPolicy,
      KitchenStartPolicy kitchenStartPolicy
  ) {
    this.orderRepository = orderRepository;
    this.transitionPolicy = transitionPolicy;
    this.kitchenStartPolicy = kitchenStartPolicy;
  }

  @Override
  public WorkflowStepResult receive(ReceivePaymentCommand command) {
    if (command == null || command.orderId() == null) {
      throw new ValidationException("orderId must be provided");
    }

    Order existing = orderRepository.findById(command.orderId())
        .orElseThrow(() -> new NotFoundException("Order not found: " + command.orderId()));

    Order updated = existing.markPaid(transitionPolicy);
    Order saved = orderRepository.save(updated);
    PaymentReceivedEvent event = new PaymentReceivedEvent(saved.id());

    return new WorkflowStepResult(
        saved.id(),
        "ReceivePayment",
        event.eventName(),
        OrderResult.fromDomain(saved),
        OrderBoardReadModel.fromDomain(saved),
        kitchenStartPolicy.react(event)
    );
  }
}
