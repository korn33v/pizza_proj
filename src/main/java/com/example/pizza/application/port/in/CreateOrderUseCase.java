package com.example.pizza.application.port.in;

import com.example.pizza.application.port.in.command.CreateOrderCommand;
import com.example.pizza.application.port.in.result.OrderResult;

// [SEMINAR-02] DIP: inbound порт (use case interface) живёт в application.
// Внешний мир (web) зависит от него, а не наоборот.
public interface CreateOrderUseCase {
  OrderResult create(CreateOrderCommand command);
}
