package com.example.pizza.application.port.in;

import com.example.pizza.application.port.in.result.OrderSummaryResult;

import java.util.List;

// [SEMINAR-22] Новый use case добавляется через inbound порт, а не через прямой вызов JPA из контроллера.
// Это сохраняет границы: web -> application(port) -> interactor.
public interface ListOrdersUseCase {
  List<OrderSummaryResult> list();
}
