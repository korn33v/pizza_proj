package com.example.pizza.application.service;

import com.example.pizza.application.port.in.command.CancelOrderCommand;
import com.example.pizza.application.port.out.OrderRepositoryPort;
import com.example.pizza.application.service.exception.*;
import com.example.pizza.domain.model.*;
import com.example.pizza.domain.policy.OrderStatusTransitionPolicy;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RemoveOrderInteractorTest {
  private final OrderRepositoryPort repo = mock(OrderRepositoryPort.class);
  private final RemoveOrderInteractor interactor = new RemoveOrderInteractor(repo, new OrderStatusTransitionPolicy());
  private final UUID id = UUID.randomUUID();

  private Order order(OrderStatus status) {
    return new Order(id, List.of(new PizzaItem("Margherita", PizzaSize.MEDIUM, 1)),
        new Address("Amsterdam", "Damrak", "1", "12", "1012"), status,
        Money.eur(BigDecimal.TEN), "CENTER");
  }

  @Test
  void removesEligibleOrdersWithoutSavingCancellation() {
    for (OrderStatus status : List.of(OrderStatus.CREATED, OrderStatus.PAID, OrderStatus.COOKING)) {
      reset(repo);
      when(repo.findById(id)).thenReturn(Optional.of(order(status)));
      interactor.remove(new CancelOrderCommand(id, "Mistake"));
      verify(repo).remove(id);
      verify(repo, never()).save(any());
    }
  }

  @Test
  void rejectsForbiddenStatuses() {
    for (OrderStatus status : List.of(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.DELIVERED, OrderStatus.CANCELLED)) {
      when(repo.findById(id)).thenReturn(Optional.of(order(status)));
      assertThrows(BusinessRuleViolationException.class,
          () -> interactor.remove(new CancelOrderCommand(id, "Mistake")));
    }
    verify(repo, never()).remove(any());
  }

  @Test
  void canRemovePreviouslyCancelledOrderWithSameReason() {
    when(repo.findById(id)).thenReturn(Optional.of(order(OrderStatus.CREATED)
        .cancel("Mistake", new OrderStatusTransitionPolicy())));
    interactor.remove(new CancelOrderCommand(id, " Mistake "));
    verify(repo).remove(id);
  }

  @Test
  void rejectsDifferentReasonForCancelledOrder() {
    when(repo.findById(id)).thenReturn(Optional.of(order(OrderStatus.CREATED)
        .cancel("Mistake", new OrderStatusTransitionPolicy())));
    assertThrows(BusinessRuleViolationException.class,
        () -> interactor.remove(new CancelOrderCommand(id, "Other")));
    verify(repo, never()).remove(any());
  }

  @Test
  void validatesParametersBeforeReadingData() {
    assertThrows(ValidationException.class, () -> interactor.remove(null));
    assertThrows(ValidationException.class, () -> interactor.remove(new CancelOrderCommand(null, "Mistake")));
    for (String reason : new String[]{null, "", " \n", "x".repeat(1001)}) {
      assertThrows(ValidationException.class, () -> interactor.remove(new CancelOrderCommand(id, reason)));
    }
    verifyNoInteractions(repo);
  }

  @Test
  void missingOrderIsNotFound() {
    when(repo.findById(id)).thenReturn(Optional.empty());
    assertThrows(NotFoundException.class, () -> interactor.remove(new CancelOrderCommand(id, "Mistake")));
    verify(repo, never()).remove(any());
  }
}
