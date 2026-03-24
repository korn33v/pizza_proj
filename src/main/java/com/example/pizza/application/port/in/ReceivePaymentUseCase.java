package com.example.pizza.application.port.in;

import com.example.pizza.application.port.in.command.ReceivePaymentCommand;
import com.example.pizza.application.port.in.result.WorkflowStepResult;

public interface ReceivePaymentUseCase {
  WorkflowStepResult receive(ReceivePaymentCommand command);
}
