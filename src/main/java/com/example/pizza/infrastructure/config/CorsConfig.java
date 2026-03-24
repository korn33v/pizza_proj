package com.example.pizza.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// [SEMINAR-21] Web UI интеграция: CORS — инфраструктурная настройка.
// Важно: application/domain об этом ничего не знают; это ответственность внешнего слоя.
@Configuration
public class CorsConfig implements WebMvcConfigurer {

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
        .allowedOrigins(
            "http://localhost:5173",
            "http://localhost:3000"
        )
        .allowedMethods("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS")
        .allowedHeaders("*");
  }

  // FAQ:
  // Q: Почему CORS конфиг в infrastructure?
  // A: Это web-инфраструктура. Домен/приложение не должны знать про браузер и origin.
  // Q: Почему не ставим '*'?
  // A: Для учебного проекта можно, но лучше привычка: ограничиваем origins.
  // Q: Что будет в production?
  // A: Часто фронт и бэк сидят за одним доменом/прокси, и CORS может не понадобиться.
}
