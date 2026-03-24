package com.example.pizza.application.port.in;

import com.example.pizza.application.port.in.command.AssignCourierCommand;
import com.example.pizza.application.port.in.result.WorkflowStepResult;

public interface AssignCourierUseCase {
  WorkflowStepResult assign(AssignCourierCommand command);
}
