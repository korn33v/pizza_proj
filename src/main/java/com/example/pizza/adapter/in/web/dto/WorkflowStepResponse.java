package com.example.pizza.adapter.in.web.dto;

import com.example.pizza.application.port.in.result.OrderBoardReadModel;
import com.example.pizza.application.port.in.result.WorkflowStepResult;

import java.math.BigDecimal;
import java.util.UUID;

public record WorkflowStepResponse(
    UUID orderId,
    String executedCommand,
    String emittedEvent,
    String currentStatus,
    String policyName,
    String suggestedNextCommand,
    String explanation,
    BoardViewResponse board
) {
  public static WorkflowStepResponse fromResult(WorkflowStepResult result) {
    return new WorkflowStepResponse(
        result.orderId(),
        result.executedCommand(),
        result.emittedEvent(),
        result.order().status(),
        result.policyHint().policyName(),
        result.policyHint().suggestedNextCommand(),
        result.policyHint().explanation(),
        BoardViewResponse.fromReadModel(result.boardReadModel())
    );
  }

  public record BoardViewResponse(
      UUID id,
      String currentStatus,
      BigDecimal totalAmount,
      String currency,
      String deliveryZone,
      boolean paymentReceived,
      boolean cookingStarted,
      boolean courierAssigned,
      boolean delivered,
      String nextSuggestedCommand
  ) {
    public static BoardViewResponse fromReadModel(OrderBoardReadModel model) {
      return new BoardViewResponse(
          model.id(),
          model.currentStatus(),
          model.totalAmount(),
          model.currency(),
          model.deliveryZone(),
          model.paymentReceived(),
          model.cookingStarted(),
          model.courierAssigned(),
          model.delivered(),
          model.nextSuggestedCommand()
      );
    }
  }
}
