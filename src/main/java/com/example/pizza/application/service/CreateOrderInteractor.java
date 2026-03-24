package com.example.pizza.application.service;

import com.example.pizza.application.port.in.CreateOrderUseCase;
import com.example.pizza.application.port.in.command.CreateOrderCommand;
import com.example.pizza.application.port.in.result.OrderResult;
import com.example.pizza.application.port.out.DeliveryZonePort;
import com.example.pizza.application.port.out.OrderRepositoryPort;
import com.example.pizza.application.service.exception.ValidationException;
import com.example.pizza.domain.model.Address;
import com.example.pizza.domain.model.Money;
import com.example.pizza.domain.model.Order;
import com.example.pizza.domain.model.OrderStatus;
import com.example.pizza.domain.model.PizzaItem;
import com.example.pizza.domain.model.PizzaSize;
import com.example.pizza.domain.policy.OrderPricingPolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

// [SEMINAR-03] Application без Spring: никакого @Service/@Component/@Transactional — это чистая Java.
// Зачем: interactor тестируется JUnit+Mockito без поднятия Spring-контекста.
public final class CreateOrderInteractor implements CreateOrderUseCase {

  private final OrderRepositoryPort orderRepository;
  private final DeliveryZonePort deliveryZonePort;
  private final OrderPricingPolicy pricingPolicy;

  public CreateOrderInteractor(
      OrderRepositoryPort orderRepository,
      DeliveryZonePort deliveryZonePort,
      OrderPricingPolicy pricingPolicy
  ) {
    this.orderRepository = orderRepository;
    this.deliveryZonePort = deliveryZonePort;
    this.pricingPolicy = pricingPolicy;
  }

  @Override
  public OrderResult create(CreateOrderCommand command) {
    validate(command);

    Address address = toDomain(command.address());
    List<PizzaItem> items = toDomainItems(command.items());

    String zone = deliveryZonePort.resolveZone(address);
    Money total = pricingPolicy.calculateTotal(items);

    Order order = new Order(
        UUID.randomUUID(),
        items,
        address,
        OrderStatus.CREATED,
        total,
        zone
    );

    Order saved = orderRepository.save(order);
    return OrderResult.fromDomain(saved);
  }

  private void validate(CreateOrderCommand command) {
    if (command == null) {
      throw new ValidationException("command must be provided");
    }
    if (command.items() == null || command.items().isEmpty()) {
      throw new ValidationException("items must be non-empty");
    }
    if (command.address() == null) {
      throw new ValidationException("address must be provided");
    }
    for (CreateOrderCommand.PizzaItemCommand item : command.items()) {
      if (item == null) {
        throw new ValidationException("item must be non-null");
      }
      if (item.name() == null || item.name().isBlank()) {
        throw new ValidationException("item.name must be non-blank");
      }
      if (item.size() == null || item.size().isBlank()) {
        throw new ValidationException("item.size must be non-blank");
      }
      if (item.qty() <= 0) {
        throw new ValidationException("item.qty must be > 0");
      }
    }
    CreateOrderCommand.AddressCommand a = command.address();
    if (a.city() == null || a.city().isBlank()) throw new ValidationException("address.city must be non-blank");
    if (a.street() == null || a.street().isBlank()) throw new ValidationException("address.street must be non-blank");
    if (a.house() == null || a.house().isBlank()) throw new ValidationException("address.house must be non-blank");
    if (a.postcode() == null || a.postcode().isBlank()) throw new ValidationException("address.postcode must be non-blank");
  }

  private Address toDomain(CreateOrderCommand.AddressCommand a) {
    try {
      return new Address(a.city(), a.street(), a.house(), a.apartment(), a.postcode());
    } catch (IllegalArgumentException ex) {
      throw new ValidationException(ex.getMessage());
    }
  }

  private List<PizzaItem> toDomainItems(List<CreateOrderCommand.PizzaItemCommand> items) {
    List<PizzaItem> result = new ArrayList<>();
    for (CreateOrderCommand.PizzaItemCommand i : items) {
      PizzaSize size;
      try {
        size = PizzaSize.valueOf(i.size().trim().toUpperCase(Locale.ROOT));
      } catch (Exception ex) {
        throw new ValidationException("Unknown size: " + i.size() + " (expected SMALL/MEDIUM/LARGE)");
      }

      try {
        result.add(new PizzaItem(i.name().trim(), size, i.qty()));
      } catch (IllegalArgumentException ex) {
        throw new ValidationException(ex.getMessage());
      }
    }
    return List.copyOf(result);
  }

  // [SEMINAR-13] ANTIPATTERN: "Use case дергает Spring напрямую"
  // Плохо: если здесь появится RestTemplate/WebClient/JpaRepository, interactor станет нетестируемым и привязанным к инфраструктуре.

  // FAQ:
  // Q: Почему не @Transactional тут?
  // A: Транзакции — инфраструктурная политика. Их граница в persistence-адаптере. См. [SEMINAR-18].
  // Q: Почему command не доменная модель?
  // A: Command — контракт application boundary. Домен может эволюционировать отдельно от внешних входов.
  // Q: Почему pricingPolicy — зависимость?
  // A: DIP + тестируемость: можно заменить правило цены без переписывания use case.
}
