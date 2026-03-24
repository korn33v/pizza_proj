package com.example.pizza.adapter.out.persistence;

import com.example.pizza.adapter.out.persistence.entity.OrderEntity;
import com.example.pizza.adapter.out.persistence.mapper.OrderMapper;
import com.example.pizza.adapter.out.persistence.repository.OrderJpaRepository;
import com.example.pizza.application.port.out.OrderRepositoryPort;
import com.example.pizza.domain.model.Order;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

// [SEMINAR-18] Транзакции живут во внешнем слое.
// Application "не знает" про @Transactional — мы ставим границу там, где есть инфраструктура (JPA).
public class OrderPersistenceAdapter implements OrderRepositoryPort {

  private final OrderJpaRepository jpaRepository;
  private final OrderMapper mapper;

  public OrderPersistenceAdapter(OrderJpaRepository jpaRepository, OrderMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  @Transactional
  public Order save(Order order) {
    // Для надёжности обновляем existing entity, если она уже есть.
    OrderEntity entity = jpaRepository.findById(order.id()).orElseGet(OrderEntity::new);
    mapper.applyToEntity(entity, order);

    OrderEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Order> findById(UUID id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }


  @Override
  @Transactional(readOnly = true)
  public List<Order> findAll() {
    return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
  }

  // FAQ:
  // Q: Почему @Transactional здесь, а не в interactor?
  // A: Потому что interactor — чистая Java. Транзакции завязаны на Spring/JPA => внешний слой. См. [SEMINAR-18].
  // Q: Почему адаптер реализует порт?
  // A: DIP: порт принадлежит application. Адаптер — деталь, которую подключаем в wiring. См. [SEMINAR-02], [SEMINAR-16].
  // Q: Почему возвращаем domain Order, а не entity?
  // A: Чтобы не протекали persistence-детали. См. [SEMINAR-06], [SEMINAR-14].
}
