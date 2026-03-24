package com.example.pizza.application.port.in;

import com.example.pizza.application.port.in.command.CreateOrderCommand;
import com.example.pizza.application.port.in.result.WorkflowStepResult;

public interface CreateOrderWorkflowUseCase {
  WorkflowStepResult create(CreateOrderCommand command);
}
