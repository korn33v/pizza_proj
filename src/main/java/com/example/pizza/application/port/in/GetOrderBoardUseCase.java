package com.example.pizza.application.port.in;

import com.example.pizza.application.port.in.command.GetOrderBoardQuery;
import com.example.pizza.application.port.in.result.OrderBoardReadModel;

public interface GetOrderBoardUseCase {
  OrderBoardReadModel get(GetOrderBoardQuery query);
}
