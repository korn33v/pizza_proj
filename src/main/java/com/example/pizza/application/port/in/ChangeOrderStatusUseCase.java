package com.example.pizza.application.port.in;

import com.example.pizza.application.port.in.command.ChangeOrderStatusCommand;
import com.example.pizza.application.port.in.result.OrderResult;

public interface ChangeOrderStatusUseCase {
  OrderResult changeStatus(ChangeOrderStatusCommand command);
}
