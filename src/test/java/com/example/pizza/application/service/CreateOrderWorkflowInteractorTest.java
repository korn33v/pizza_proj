package com.example.pizza.application.service;

import com.example.pizza.application.port.in.command.CreateOrderCommand;
import com.example.pizza.application.port.out.DeliveryZonePort;
import com.example.pizza.application.port.out.OrderRepositoryPort;
import com.example.pizza.application.service.policy.PaymentRequestedPolicy;
import com.example.pizza.domain.model.Address;
import com.example.pizza.domain.model.Order;
import com.example.pizza.domain.policy.OrderPricingPolicy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreateOrderWorkflowInteractorTest {

  @Test
  void create_returnsEventAndPolicyHint() {
    OrderRepositoryPort repo = mock(OrderRepositoryPort.class);
    DeliveryZonePort zonePort = mock(DeliveryZonePort.class);

    when(zonePort.resolveZone(any(Address.class))).thenReturn("CENTER");
    when(repo.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

    var interactor = new CreateOrderWorkflowInteractor(repo, zonePort, new OrderPricingPolicy(), new PaymentRequestedPolicy());

    var result = interactor.create(new CreateOrderCommand(
        new CreateOrderCommand.AddressCommand("Amsterdam", "Damrak", "1", "12", "1012"),
        List.of(new CreateOrderCommand.PizzaItemCommand("Margherita", "MEDIUM", 1))
    ));

    assertEquals("CreateOrder", result.executedCommand());
    assertEquals("OrderCreated", result.emittedEvent());
    assertEquals("ReceivePayment", result.policyHint().suggestedNextCommand());
    assertEquals("CREATED", result.order().status());
  }
}
