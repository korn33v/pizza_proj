package com.example.pizza;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// [SEMINAR-01] Границы слоёв: Spring Boot стартует "снаружи" и тянет внутрь зависимости только через wiring.
// Важно: домен/приложение НЕ должны тянуть Spring обратно — направление зависимостей всегда к центру (domain).
@SpringBootApplication
public class PizzaDeliveryApplication {
  public static void main(String[] args) {
    SpringApplication.run(PizzaDeliveryApplication.class, args);
  }
}
