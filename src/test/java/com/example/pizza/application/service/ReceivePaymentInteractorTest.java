package com.example.pizza.application.service;

import com.example.pizza.application.port.in.command.ReceivePaymentCommand;
import com.example.pizza.application.port.out.OrderRepositoryPort;
import com.example.pizza.application.service.policy.KitchenStartPolicy;
import com.example.pizza.domain.model.Address;
import com.example.pizza.domain.model.Money;
import com.example.pizza.domain.model.Order;
import com.example.pizza.domain.model.OrderStatus;
import com.example.pizza.domain.model.PizzaItem;
import com.example.pizza.domain.model.PizzaSize;
import com.example.pizza.domain.policy.OrderStatusTransitionPolicy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReceivePaymentInteractorTest {

  @Test
  void receivePayment_emitsPaymentReceivedAndSuggestsStartCooking() {
    OrderRepositoryPort repo = mock(OrderRepositoryPort.class);
    UUID id = UUID.randomUUID();

    Order existing = new Order(
        id,
        List.of(new PizzaItem("Margherita", PizzaSize.MEDIUM, 1)),
        new Address("Amsterdam", "Damrak", "1", "12", "1012"),
        OrderStatus.CREATED,
        Money.eur(new BigDecimal("10.00")),
        "CENTER"
    );

    when(repo.findById(id)).thenReturn(Optional.of(existing));
    when(repo.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

    var interactor = new ReceivePaymentInteractor(repo, new OrderStatusTransitionPolicy(), new KitchenStartPolicy());

    var result = interactor.receive(new ReceivePaymentCommand(id));

    assertEquals("ReceivePayment", result.executedCommand());
    assertEquals("PaymentReceived", result.emittedEvent());
    assertEquals("PAID", result.order().status());
    assertEquals("StartCooking", result.policyHint().suggestedNextCommand());
  }
}
