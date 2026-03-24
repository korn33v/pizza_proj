package com.example.pizza.application.port.in;

import com.example.pizza.application.port.in.command.StartCookingCommand;
import com.example.pizza.application.port.in.result.WorkflowStepResult;

public interface StartCookingUseCase {
  WorkflowStepResult start(StartCookingCommand command);
}
