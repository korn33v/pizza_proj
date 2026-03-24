package com.example.pizza.application.port.in;

import com.example.pizza.application.port.in.command.GetOrderQuery;
import com.example.pizza.application.port.in.result.OrderResult;

public interface GetOrderUseCase {
  OrderResult get(GetOrderQuery query);
}
