package com.example.pizza.application.port.in;

import com.example.pizza.application.port.in.command.CancelOrderCommand;

public interface RemoveOrderUseCase {
  void remove(CancelOrderCommand command);
}
