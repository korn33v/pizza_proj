package com.example.pizza.application.service;

import com.example.pizza.application.port.in.command.CreateOrderCommand;
import com.example.pizza.application.port.out.DeliveryZonePort;
import com.example.pizza.application.port.out.OrderRepositoryPort;
import com.example.pizza.application.service.exception.ValidationException;
import com.example.pizza.domain.model.Address;
import com.example.pizza.domain.model.Order;
import com.example.pizza.domain.policy.OrderPricingPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// [SEMINAR-15] Как тестируем use case моками:
// - мокируем outbound-порты (OrderRepositoryPort, DeliveryZonePort)
// - НЕ мокируем Spring Data репозиторий, потому что use case про него знать не должен
@ExtendWith(MockitoExtension.class)
class CreateOrderInteractorTest {

  @Test
  void createOrder_happyPath_persistsAndReturnsResult() {
    OrderRepositoryPort repo = mock(OrderRepositoryPort.class);
    DeliveryZonePort zonePort = mock(DeliveryZonePort.class);

    when(zonePort.resolveZone(any(Address.class))).thenReturn("CENTER");
    when(repo.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

    CreateOrderInteractor interactor = new CreateOrderInteractor(repo, zonePort, new OrderPricingPolicy());

    CreateOrderCommand cmd = new CreateOrderCommand(
        new CreateOrderCommand.AddressCommand("Amsterdam", "Damrak", "1", "12", "1012"),
        List.of(
            new CreateOrderCommand.PizzaItemCommand("Margherita", "MEDIUM", 2),
            new CreateOrderCommand.PizzaItemCommand("Pepperoni", "LARGE", 1)
        )
    );

    var result = interactor.create(cmd);

    assertNotNull(result.id());
    assertEquals("CREATED", result.status());
    assertEquals("CENTER", result.deliveryZone());
    assertEquals("EUR", result.totalPrice().currency());
    assertEquals(2, result.items().size());

    // Что проверяем на этом уровне:
    // - use case вызывает внешние порты
    // - сохраняет в репозиторий
    // - возвращает корректный результат
    // Что НЕ тестируем:
    // - SQL, Hibernate mapping, Flyway (это интеграционные тесты)
    verify(zonePort, times(1)).resolveZone(any(Address.class));

    ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
    verify(repo, times(1)).save(orderCaptor.capture());

    Order saved = orderCaptor.getValue();
    assertEquals("CENTER", saved.deliveryZone());
    assertEquals("EUR", saved.totalPrice().currency());
    // 2*10 + 1*12 = 32.00
    assertEquals("32.00", saved.totalPrice().amount().toPlainString());
  }

  @Test
  void createOrder_rejectsEmptyItems() {
    OrderRepositoryPort repo = mock(OrderRepositoryPort.class);
    DeliveryZonePort zonePort = mock(DeliveryZonePort.class);

    CreateOrderInteractor interactor = new CreateOrderInteractor(repo, zonePort, new OrderPricingPolicy());

    CreateOrderCommand cmd = new CreateOrderCommand(
        new CreateOrderCommand.AddressCommand("Amsterdam", "Damrak", "1", "12", "1012"),
        List.of()
    );

    assertThrows(ValidationException.class, () -> interactor.create(cmd));

    verifyNoInteractions(repo);
    verifyNoInteractions(zonePort);
  }
}
