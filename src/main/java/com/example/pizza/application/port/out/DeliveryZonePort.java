package com.example.pizza.application.port.out;

import com.example.pizza.domain.model.Address;

// [SEMINAR-11] Как заменить внешнюю интеграцию без правок use case:
// use case знает только порт, а реализация живёт в adapter/out.
public interface DeliveryZonePort {
  String resolveZone(Address address);
}
