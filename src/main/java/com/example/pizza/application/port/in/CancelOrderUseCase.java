package com.example.pizza.application.port.in;

import com.example.pizza.application.port.in.command.CancelOrderCommand;

public interface CancelOrderUseCase {
  void cancel(CancelOrderCommand command);
}
