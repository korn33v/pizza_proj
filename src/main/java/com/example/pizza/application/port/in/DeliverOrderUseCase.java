package com.example.pizza.application.port.in;

import com.example.pizza.application.port.in.command.DeliverOrderCommand;
import com.example.pizza.application.port.in.result.WorkflowStepResult;

public interface DeliverOrderUseCase {
  WorkflowStepResult deliver(DeliverOrderCommand command);
}
