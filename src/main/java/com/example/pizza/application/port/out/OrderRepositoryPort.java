package com.example.pizza.application.port.out;

import com.example.pizza.domain.model.Order;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface OrderRepositoryPort {
  Order save(Order order);
  void remove(UUID id);
  Optional<Order> findById(UUID id);
  List<Order> findAll();
}
