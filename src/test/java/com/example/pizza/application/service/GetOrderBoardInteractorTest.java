package com.example.pizza.application.service;

import com.example.pizza.application.port.in.command.GetOrderBoardQuery;
import com.example.pizza.application.port.out.OrderRepositoryPort;
import com.example.pizza.domain.model.Address;
import com.example.pizza.domain.model.Money;
import com.example.pizza.domain.model.Order;
import com.example.pizza.domain.model.OrderStatus;
import com.example.pizza.domain.model.PizzaItem;
import com.example.pizza.domain.model.PizzaSize;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class GetOrderBoardInteractorTest {

  @Test
  void boardReadModel_showsWorkflowState() {
    OrderRepositoryPort repo = mock(OrderRepositoryPort.class);
    UUID id = UUID.randomUUID();

    Order existing = new Order(
        id,
        List.of(new PizzaItem("Margherita", PizzaSize.MEDIUM, 1)),
        new Address("Amsterdam", "Damrak", "1", "12", "1012"),
        OrderStatus.OUT_FOR_DELIVERY,
        Money.eur(new BigDecimal("10.00")),
        "CENTER"
    );

    when(repo.findById(id)).thenReturn(Optional.of(existing));

    var interactor = new GetOrderBoardInteractor(repo);
    var result = interactor.get(new GetOrderBoardQuery(id));

    assertEquals("OUT_FOR_DELIVERY", result.currentStatus());
    assertTrue(result.paymentReceived());
    assertTrue(result.cookingStarted());
    assertTrue(result.courierAssigned());
    assertEquals("DeliverOrder", result.nextSuggestedCommand());
  }
}
