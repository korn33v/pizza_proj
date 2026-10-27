package com.example.pizza.adapter.in.web;

import com.example.pizza.adapter.in.web.dto.ChangeStatusRequest;
import com.example.pizza.adapter.in.web.dto.CreateOrderRequest;
import com.example.pizza.adapter.in.web.dto.OrderResponse;
import com.example.pizza.adapter.in.web.dto.PizzaItemDto;
import com.example.pizza.application.port.in.ChangeOrderStatusUseCase;
import com.example.pizza.application.port.in.CreateOrderUseCase;
import com.example.pizza.application.port.in.GetOrderUseCase;
import com.example.pizza.application.port.in.ListOrdersUseCase;
import com.example.pizza.application.port.in.command.ChangeOrderStatusCommand;
import com.example.pizza.application.port.in.command.CreateOrderCommand;
import com.example.pizza.application.port.in.command.GetOrderQuery;
import com.example.pizza.application.port.in.result.OrderResult;
import com.example.pizza.application.port.in.CancelOrderUseCase;
import com.example.pizza.application.port.in.command.CancelOrderCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.math.BigDecimal;
import java.util.UUID;

// [SEMINAR-10] Тонкий контроллер: максимум — маппинг DTO<->command/result и HTTP-коды.
// Бизнес-логика (цены, статусы, зоны) — НЕ здесь.
@RestController
@RequestMapping("/orders")
public class OrderController {

  private final CreateOrderUseCase createOrderUseCase;
  private final ChangeOrderStatusUseCase changeOrderStatusUseCase;
  private final GetOrderUseCase getOrderUseCase;
  private final ListOrdersUseCase listOrdersUseCase;
  private final CancelOrderUseCase cancelOrderUseCase;

  public OrderController(
      CreateOrderUseCase createOrderUseCase,
      ChangeOrderStatusUseCase changeOrderStatusUseCase,
      GetOrderUseCase getOrderUseCase,
      ListOrdersUseCase listOrdersUseCase,
      CancelOrderUseCase cancelOrderUseCase
  ) {
    this.createOrderUseCase = createOrderUseCase;
    this.changeOrderStatusUseCase = changeOrderStatusUseCase;
    this.getOrderUseCase = getOrderUseCase;
    this.listOrdersUseCase = listOrdersUseCase;
    this.cancelOrderUseCase = cancelOrderUseCase;
  }

  @PostMapping
  public ResponseEntity<OrderResponse> create(@RequestBody CreateOrderRequest request) {
    CreateOrderCommand command = toCommand(request);
    OrderResult result = createOrderUseCase.create(command);

    OrderResponse response = toResponse(result);
    return ResponseEntity
        .created(URI.create("/orders/" + response.id()))
        .body(response);
  }

  @Operation(summary = "Отменить ошибочный заказ по обращению пользователя",
      description = "Переводит заказ в CANCELLED, сохраняя причину и историю. "
          + "Разрешено для CREATED, PAID и COOKING. Повторная отмена с той же причиной возвращает 204.")
  @ApiResponse(responseCode = "204", description = "Заказ отменён")
  @ApiResponse(responseCode = "400", description = "Некорректный UUID или причина (нужно от 1 до 1000 символов)")
  @ApiResponse(responseCode = "404", description = "Заказ не найден")
  @ApiResponse(responseCode = "409", description = "Отмена запрещена для текущего статуса или уже сохранена другая причина")
  @DeleteMapping("/{orderId}")
  public ResponseEntity<Void> cancel(
      @Parameter(description = "Номер заказа (UUID)", required = true)
      @PathVariable("orderId") UUID orderId,
      @Parameter(description = "Причина отмены, от 1 до 1000 символов", required = true)
      @RequestParam("reason") String reason) {
    cancelOrderUseCase.cancel(new CancelOrderCommand(orderId, reason));
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/status")
  public ResponseEntity<OrderResponse> changeStatus(@PathVariable("id") UUID id, @RequestBody ChangeStatusRequest request) {
    ChangeOrderStatusCommand command = new ChangeOrderStatusCommand(id, request == null ? null : request.status());
    OrderResult result = changeOrderStatusUseCase.changeStatus(command);
    return ResponseEntity.ok(toResponse(result));
  }


  @GetMapping
  public ResponseEntity<List<OrderSummaryResponse>> list() {
    var list = listOrdersUseCase.list().stream().map(OrderSummaryResponse::fromResult).toList();
    return ResponseEntity.ok(list);
  }

  @GetMapping("/{id}")
  public ResponseEntity<OrderResponse> get(@PathVariable("id") UUID id) {
    OrderResult result = getOrderUseCase.get(new GetOrderQuery(id));
    return ResponseEntity.ok(toResponse(result));
  }

  private static CreateOrderCommand toCommand(CreateOrderRequest request) {
    if (request == null) {
      throw new IllegalArgumentException("request must be provided");
    }
    CreateOrderCommand.AddressCommand address = new CreateOrderCommand.AddressCommand(
        request.address() == null ? null : request.address().city(),
        request.address() == null ? null : request.address().street(),
        request.address() == null ? null : request.address().house(),
        request.address() == null ? null : request.address().apartment(),
        request.address() == null ? null : request.address().postcode()
    );

    List<CreateOrderCommand.PizzaItemCommand> items = (request.items() == null ? List.<PizzaItemDto>of() : request.items())
        .stream()
        .map(i -> new CreateOrderCommand.PizzaItemCommand(
            i == null ? null : i.name(),
            i == null ? null : i.size(),
            i == null ? 0 : i.qty()
        ))
        .toList();

    return new CreateOrderCommand(address, items);
  }

  private static OrderResponse toResponse(OrderResult result) {
    return new OrderResponse(
        result.id(),
        new com.example.pizza.adapter.in.web.dto.AddressDto(
            result.address().city(),
            result.address().street(),
            result.address().house(),
            result.address().apartment(),
            result.address().postcode()
        ),
        result.items().stream()
            .map(i -> new PizzaItemDto(i.name(), i.size(), i.qty()))
            .toList(),
        result.status(),
        result.totalPrice().amount(),
        result.totalPrice().currency(),
        result.deliveryZone(),
        result.cancellationReason()
    );
  }

  // [SEMINAR-20] ANTIPATTERN: "Жирный контроллер"
  // Плохо: если сюда добавить расчёт цен/переходы статусов/вызовы репозитория — получите не тестируемый комбайн,
  // который невозможно переиспользовать из другого входа (например, Kafka consumer).

  // FAQ:
  // Q: Почему контроллер зависит от CreateOrderUseCase интерфейса, а не от CreateOrderInteractor?
  // A: DIP: контроллер — внешний слой и должен зависеть от абстракции application. См. [SEMINAR-02].
  // Q: Почему здесь нет @Transactional?
  // A: Контроллер — входной адаптер. Транзакции — инфраструктура (см. [SEMINAR-18]).
  // Q: Почему маппинг вручную?
  // A: Учебный проект: хотим явно видеть границы и места маппинга.


  // Лёгкий HTTP-ответ для таблицы в UI.
  // [SEMINAR-22] DTO живёт в web-адаптере: мы не тащим его в domain/application.
  public record OrderSummaryResponse(
      java.util.UUID id,
      String status,
      BigDecimal totalAmount,
      String currency,
      String deliveryZone
  ) {
    static OrderSummaryResponse fromResult(com.example.pizza.application.port.in.result.OrderSummaryResult r) {
      return new OrderSummaryResponse(r.id(), r.status(), r.totalAmount(), r.currency(), r.deliveryZone());
    }
  }

}
