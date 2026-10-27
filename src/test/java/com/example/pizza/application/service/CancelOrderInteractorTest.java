package com.example.pizza.application.service;

import com.example.pizza.application.port.in.command.CancelOrderCommand;
import com.example.pizza.application.port.out.OrderRepositoryPort;
import com.example.pizza.application.service.exception.BusinessRuleViolationException;
import com.example.pizza.application.service.exception.NotFoundException;
import com.example.pizza.application.service.exception.ValidationException;
import com.example.pizza.adapter.out.persistence.entity.OrderEntity;
import com.example.pizza.adapter.out.persistence.mapper.OrderMapper;
import com.example.pizza.domain.model.*;
import com.example.pizza.domain.policy.OrderStatusTransitionPolicy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CancelOrderInteractorTest {
  private final OrderRepositoryPort repo = mock(OrderRepositoryPort.class);
  private final CancelOrderInteractor interactor = new CancelOrderInteractor(repo, new OrderStatusTransitionPolicy());
  private final UUID id = UUID.randomUUID();

  private Order order(OrderStatus status) {
    return new Order(id, List.of(new PizzaItem("Margherita", PizzaSize.MEDIUM, 1)),
        new Address("Amsterdam", "Damrak", "1", "12", "1012"), status,
        Money.eur(new BigDecimal("10.00")), "CENTER");
  }

  @Test
  void cancelsEligibleStatusesAndPreservesOrderDetails() {
    for (OrderStatus status : List.of(OrderStatus.CREATED, OrderStatus.PAID, OrderStatus.COOKING)) {
      reset(repo);
      Order existing = order(status);
      when(repo.findById(id)).thenReturn(Optional.of(existing));
      interactor.cancel(new CancelOrderCommand(id, "  Ошибочный заказ  "));
      verify(repo).save(new Order(id, existing.items(), existing.address(), OrderStatus.CANCELLED,
          existing.totalPrice(), existing.deliveryZone(), "Ошибочный заказ"));
    }
  }

  @Test
  void rejectsLateCancellationAndDoesNotPersist() {
    for (OrderStatus status : List.of(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.DELIVERED, OrderStatus.CANCELLED)) {
      when(repo.findById(id)).thenReturn(Optional.of(order(status)));
      assertThrows(BusinessRuleViolationException.class,
          () -> interactor.cancel(new CancelOrderCommand(id, "Mistake")));
    }
    verify(repo, never()).save(any());
  }

  @Test
  void validatesReasonBeforeAccessingRepository() {
    for (String reason : new String[]{null, "", " \t\n", "x".repeat(1001)}) {
      assertThrows(ValidationException.class, () -> interactor.cancel(new CancelOrderCommand(id, reason)));
    }
    assertThrows(ValidationException.class, () -> interactor.cancel(null));
    assertThrows(ValidationException.class, () -> interactor.cancel(new CancelOrderCommand(null, "Mistake")));
    verifyNoInteractions(repo);
  }

  @Test
  void acceptsMaximumReasonLength() {
    when(repo.findById(id)).thenReturn(Optional.of(order(OrderStatus.CREATED)));
    interactor.cancel(new CancelOrderCommand(id, "x".repeat(1000)));
    verify(repo).save(argThat(o -> o.cancellationReason().length() == 1000));
  }

  @Test
  void missingOrderIsNotFound() {
    when(repo.findById(id)).thenReturn(Optional.empty());
    assertThrows(NotFoundException.class, () -> interactor.cancel(new CancelOrderCommand(id, "Mistake")));
    verify(repo, never()).save(any());
  }

  @Test
  void repeatedDeleteIsIdempotentAndCannotOverwriteReason() {
    Order cancelled = order(OrderStatus.CREATED).cancel("Mistake", new OrderStatusTransitionPolicy());
    when(repo.findById(id)).thenReturn(Optional.of(cancelled));
    interactor.cancel(new CancelOrderCommand(id, " Mistake "));
    assertThrows(BusinessRuleViolationException.class,
        () -> interactor.cancel(new CancelOrderCommand(id, "Another reason")));
    verify(repo, never()).save(any());
  }

  @Test
  void persistenceMappingAndStatusCopyPreserveReason() {
    Order cancelled = order(OrderStatus.CREATED).cancel("Mistake", new OrderStatusTransitionPolicy());
    OrderMapper mapper = new OrderMapper();
    OrderEntity entity = new OrderEntity();
    mapper.applyToEntity(entity, cancelled);
    assertEquals(cancelled, mapper.toDomain(entity));
    assertEquals("Mistake", cancelled.withStatus(OrderStatus.CANCELLED).cancellationReason());
    mapper.applyToEntity(entity, order(OrderStatus.CREATED));
    assertNull(mapper.toDomain(entity).cancellationReason());
  }
}
